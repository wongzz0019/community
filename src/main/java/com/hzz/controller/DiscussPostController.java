package com.hzz.controller;

import com.hzz.entity.Comment;
import com.hzz.entity.DiscussPost;
import com.hzz.entity.Page;
import com.hzz.entity.User;
import com.hzz.service.CommentService;
import com.hzz.service.DiscussPostService;
import com.hzz.service.UserService;
import com.hzz.util.CommunityUtil;
import com.hzz.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hzz.util.CommunityConstant.ENTITY_TYPE_POST;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    //增加帖子
    @PostMapping(path = "/add")
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        //用户没有登录
        if (user == null) {
            return CommunityUtil.getJSONString(403, "你还没有登录哦!");
        }
        //没有填写标题
        if (StringUtils.isBlank(title) || title==null){
            return CommunityUtil.getJSONString(-1, "请填写标题");
        }
        //没有填写内容
        if (StringUtils.isBlank(content) || content==null){
            return CommunityUtil.getJSONString(-2, "请填写内容");
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        Map<String,Object> map = new HashMap();
        map.put("title",title);
        map.put("content",content);
        // 报错的情况,将来统一处理.
        return CommunityUtil.getJSONString(0, "发布成功!",map);
    }

    @GetMapping(path = "/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        // 帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        // 作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);
        //评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail" + discussPostId);
        page.setRows(post.getCommentCount());

        List<Comment> commentList = commentService.findCommentsByEntity(ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());

        return "/site/discuss-detail";
    }

}

