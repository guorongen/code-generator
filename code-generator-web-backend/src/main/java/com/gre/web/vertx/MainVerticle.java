package com.gre.web.vertx;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gre.web.common.ResultUtils;
import com.gre.web.controller.GeneratorController;
import com.gre.web.manager.CacheManager;
import com.gre.web.model.dto.generator.GeneratorQueryRequest;
import com.gre.web.model.vo.GeneratorVO;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;

public class MainVerticle extends AbstractVerticle {

    private CacheManager cacheManager;

    public MainVerticle(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void start() throws Exception {
        vertx.createHttpServer()
                .requestHandler(req -> {
                    HttpMethod httpMethod = req.method();
                    String path = req.path();
                    if (HttpMethod.POST.equals(httpMethod) && "/generator/page".equals(path)) {
                        req.handler(buffer -> {
                            String requestBody = buffer.toString();
                            GeneratorQueryRequest generatorQueryRequest = JSONUtil.toBean(requestBody, GeneratorQueryRequest.class);
                            String cacheKey = GeneratorController.getPageCacheKey(generatorQueryRequest);

                            HttpServerResponse response = req.response();
                            response.putHeader("content-type", "application/json");

                            Object cacheValue = cacheManager.get(cacheKey);
                            if (cacheValue != null) {
                                response.end(JSONUtil.toJsonStr(ResultUtils.success((Page<GeneratorVO>) cacheValue)));
                                return;
                            }
                            response.end("");
                        });
                    }
                })
                .listen(8888)
                .onSuccess(
                        server -> {
                            System.out.println("HTTP server started on port " + server.actualPort()
                            );
                        });
    }
}
