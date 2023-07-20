package com.tanhua.admin.controller;

import com.tanhua.admin.service.ManagerService;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.vo.MovementsVo;
import com.tanhua.model.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/manage")
public class ManagerController {

    @Autowired
    private ManagerService managerService;


    @GetMapping("/users")
    public ResponseEntity users(@RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer pagesize) {
        PageResult result = managerService.findAllUsers(page,pagesize);
        return ResponseEntity.ok(result);
    }

    //根据id查询
    @GetMapping("/users/{userId}")
    public ResponseEntity findUserById(@PathVariable("userId") Long userId) {
        UserInfo userInfo = managerService.findUserById(userId);
        return ResponseEntity.ok(userInfo);
    }

    /**
     * 查询指定用户发布的所有视频列表
     */
    @GetMapping("/videos")
    public ResponseEntity videos(@RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer pagesize,
                                 Long uid ) {
        PageResult result = managerService.findAllVideos(page,pagesize,uid);
        return ResponseEntity.ok(result);
    }

    //查询动态
    @GetMapping("/messages")
    public ResponseEntity messages(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer pagesize,
                                   Long uid,Integer state) {
        PageResult result = managerService.findAllMovements(page,pagesize,uid,state);
        return ResponseEntity.ok(result);
    }

    /**
     * 动态详情
     * @param movenId 动态id
     * @return
     */
    @GetMapping("/messages/{id}")
    public ResponseEntity findXingqing(@PathVariable("id") String movenId){
        MovementsVo movementsVo=managerService.findXingqing(movenId);
        return ResponseEntity.ok(movementsVo);
    }

    @GetMapping("/messages/comments")
    public ResponseEntity findComments(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer pagesize,
                                       String messageID
                                       ){
        PageResult pr=managerService.findComments(page,pagesize,messageID);
        return ResponseEntity.ok(pr);
    }
}
