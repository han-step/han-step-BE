package com.AI.Han_Step.controller;

import com.AI.Han_Step.dto.request.MyInfoRequest;
import com.AI.Han_Step.dto.response.MyBriefingResponse;
import com.AI.Han_Step.dto.response.MyInfoResponse;
import com.AI.Han_Step.service.MyInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/me")
public class MyInfoController {

    private final MyInfoService myInfoService;

    @PostMapping
    public MyInfoResponse saveMyInfo(@RequestBody MyInfoRequest request) {
        return myInfoService.saveMyInfo(request);
    }

    @GetMapping
    public MyInfoResponse getMyInfo() {
        return myInfoService.getMyInfo();
    }

    @GetMapping("/briefing")
    public MyBriefingResponse getMyBriefing() {
        return myInfoService.getMyBriefing();
    }
}

