package com.econage.ai.support.utils;

import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.util.Assert;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.StringJoiner;

/**
 * Exception工具类
 */
public final class ExceptionMore {
	private ExceptionMore() {
	}

	/*提取代理类及反射场景中，异常信息。去掉反射异常及调用异常包装类*/
	public static Throwable unwrapThrowable(Throwable wrapped) {
		Assert.notNull(wrapped,"wrapped is empty");
		Throwable unwrapped = wrapped;
		while (true) {
			if (unwrapped instanceof InvocationTargetException) {
				unwrapped = ((InvocationTargetException) unwrapped).getTargetException();
			} else if (unwrapped instanceof UndeclaredThrowableException) {
				unwrapped = ((UndeclaredThrowableException) unwrapped).getUndeclaredThrowable();
			} else {
				return unwrapped;
			}
		}
	}

	/*
	* 循环拆解代理异常、root异常
	* */
	public static Throwable unwrapAndGetRootCause(final Throwable cause){
		Assert.notNull(cause,"cause is empty");
		Throwable preCause = cause;
		while(true){
			Throwable currCause= ExceptionUtils.getRootCause(unwrapThrowable(preCause));
			if(currCause == preCause){
				return currCause;
			}else{
				preCause = currCause;
			}
		}
	}

	/*
	 * 异常信息会自动判断，是否需要找到root异常
	 * */
	public static Throwable gracefulFindException(@NonNull Throwable cause){
		final Throwable underlyingE = unwrapAndGetRootCause(cause);

		/*
		 * 如果最底层异常明细为空，且是部分常见的运行时错误，认为应当使用初始类，并去除代理包装信息
		 * */
		if(shouldPrintStackDirect(underlyingE)){
			/*
			 * 如果打印直接异常信息，去掉常见的代理类包装信息
			 * */
			return unwrapThrowable(underlyingE);
		}

		return underlyingE;
	}

	/*
	* 异常信息会自动判断，是否需要找到root异常，自动截取足够的长度，输出信息
	* */
	public static String gracefulTruncateExceptionStack(Throwable cause){
		return truncateExceptionStack(gracefulFindException(cause));
	}

	/*
	 * 异常信息会自动判断，是否需要找到root异常，自动截取足够的长度，输出信息
	 * */
	public static String gracefulTruncateExceptionStack(Throwable cause,int truncateLen){
		return truncateExceptionStack(gracefulFindException(cause),truncateLen);
	}

	private static boolean shouldPrintStackDirect(Throwable cause){
		if(StringUtils.isEmpty(cause.getMessage())){
			return cause instanceof NullPointerException
					|| cause instanceof IllegalStateException
					|| cause instanceof UnsupportedOperationException;
		}
		return false;
	}

	private static final int EXCEPTION_LEN = 3900;

	/*
	* 日常报错，最多的就是sql错误，缩减常见的jdbc类库、连接池类库异常栈
	* */
	private static final String[] TRUNCATE_EXCEPTION_STACK_PREFIX_ARRAY = new String[]{
			"org.mariadb", "com.mysql",
			"oracle","dm.jdbc",
			"org.postgresql", "com.kingbase8","com.highgo.jdbc", "com.aliyun.polardb",
			"com.zaxxer",
			"com.econage.cornerstone.db.ecobatis",
			"org.springframework.transaction",
			"org.springframework.aop",
			"com.fasterxml.jackson.databind"
	};

	private static final String JAVA_DELEGATE_PREFIX = "    at jdk.delegating....";
	private static final String[] JAVA_DELEGATE_EXCEPTION_STACK_PREFIX_ARRAY = new String[]{
			//jdk生成的代理类
			"jdk.internal.reflect.GeneratedMethodAccessor",
			"java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl",
			"java.base/java.lang.reflect.Method.invoke"
	};

	/*
	* 缩减常见头部类库调用信息，尽可能显示业务相关调用
	* */
	static String truncateExceptionStack(Throwable cause){
		return truncateExceptionStack(cause,EXCEPTION_LEN);
	}


	static String truncateExceptionStack(Throwable cause,int truncateLen){
		Assert.notNull(cause,"cause is empty");
		return truncateExceptionStack(
				ExceptionUtils.getStackFrames(cause),
				parseExceptionMsg(cause),
				truncateLen
		);
	}


	static String truncateExceptionStack(String[] stackFrames,String expMsg,int truncateLen){
		StringJoiner stackFrameJoiner = new StringJoiner(System.lineSeparator());
		stackFrameJoiner.add("Message:"+expMsg);

		String lastStackFrame = null;

		int idx = 0;
		for(String exceptionStackFrame : stackFrames){
			idx++;
			String currStackFrame = exceptionStackFrame;

			boolean matchTruncate = false;

			//初始第一条异常，不做匹配
			if(idx>1){
				for(String truncateExpStackPrefix : TRUNCATE_EXCEPTION_STACK_PREFIX_ARRAY){
					int truncateIdx = currStackFrame.indexOf(truncateExpStackPrefix);
					if(truncateIdx>-1){
						currStackFrame = currStackFrame.substring(0,truncateIdx+truncateExpStackPrefix.length())+"...";
						matchTruncate = true;
						break;
					}
				}

				if(!matchTruncate){
					for(var javaDelegatePrefix : JAVA_DELEGATE_EXCEPTION_STACK_PREFIX_ARRAY){
						int truncateIdx = currStackFrame.indexOf(javaDelegatePrefix);
						if(truncateIdx>-1){
							currStackFrame = JAVA_DELEGATE_PREFIX;
							matchTruncate = true;
							break;
						}
					}
				}
			}

			if(matchTruncate){
				//如果与最后一次栈信息不同，添加栈
				if(!StringUtils.equals(lastStackFrame,currStackFrame)){
					stackFrameJoiner.add(currStackFrame);
					//超出上限，停止组装，当前栈会打印
					if(stackFrameJoiner.length()>truncateLen){
						break;
					}
					lastStackFrame = currStackFrame;
				}
			}else{
				stackFrameJoiner.add(currStackFrame);
			}
		}

		String exceptionFull = stackFrameJoiner.toString();
		if(exceptionFull.length()>truncateLen){
			exceptionFull = exceptionFull.substring(0,truncateLen);
		}
		return exceptionFull;
	}

	public static String parseExceptionMsg(Throwable t){
		if(t==null){
			return StringUtils.EMPTY;
		}
		return t.getMessage();
	}

}
