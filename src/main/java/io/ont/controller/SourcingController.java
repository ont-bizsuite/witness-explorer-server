package io.ont.controller;

import io.ont.bean.Result;
import io.ont.service.SourcingService;
import io.ont.utils.ErrorInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Api(tags = "Sourcing controller")
@RestController
@RequestMapping("/api/v1/witness")
@CrossOrigin
public class SourcingController {
    @Autowired
    private SourcingService sourcingService;

    @ApiOperation(value = "souring record", notes = "sourcing record", httpMethod = "POST")
    @GetMapping("/proof/{hash}")
    public Result getProof(@PathVariable String hash) throws Exception {
        String action = "getProof";
        Map<String, Object> result = sourcingService.getProof(action, hash);
        return new Result(action, ErrorInfo.SUCCESS.code(), ErrorInfo.SUCCESS.descEN(), result);
    }

    @ApiOperation(value = "souring record", notes = "sourcing record", httpMethod = "POST")
    @GetMapping("/block/{hash}")
    public Result getBlock(@PathVariable String hash) throws Exception {
        String action = "getBlock";
        Map<String, Object> result = sourcingService.getBlock(action, hash);
        return new Result(action, ErrorInfo.SUCCESS.code(), ErrorInfo.SUCCESS.descEN(), result);
    }
}
