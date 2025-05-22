package com.econage.ai.controller;

import com.econage.ai.base.response.WebResponseWrapper;
import com.econage.ai.dto.chat.ChatCompletionRequest;
import com.econage.ai.dto.vectorstore.VectorStorePushRequest;
import com.econage.ai.facade.AIIntegrateFacadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author hanpeng
 * @date 2024/9/24 14:49
 */
@RestController
@RequestMapping( "/ai/vector-store")

public class AIVectorStoreController {

    private AIIntegrateFacadeService integrateFacadeService;

    @Autowired
    void auto(
            AIIntegrateFacadeService openAiIntegrateService
    ){
        this.integrateFacadeService = openAiIntegrateService;
    }

    /**
     * 推送向量数据库
     * @return
     */
    @PostMapping(value =  "/push")
    public Object pushVectorStore(@RequestBody VectorStorePushRequest pushRequest){
        integrateFacadeService.updateVectorStore(pushRequest);
        return WebResponseWrapper.EMPTY_SUCCESS;

    }

    /**
     * 删除向量数据库
     * @return
     */
    @PostMapping(value =  "/delete")
    public Object deleteVectorStore(@RequestBody List<String> modularInnerIds){
        integrateFacadeService.deleteVectorStore(modularInnerIds);
        return WebResponseWrapper.EMPTY_SUCCESS;
    }


//    /**
//     *
//     * @return
//     */
//    @PostMapping(value =  "/add")
//    public void chatWithVectorStore1(@RequestParam String path){
//        var id = "1840687908246528000";
//        Map<String,Object> metaDataMap = Map.of("parentId", "232323");
//        integrateFacadeService.updateVectorStore(
//                new FileSystemResource(path),
//                id,
//                metaDataMap
//        );
//
//    }


}
