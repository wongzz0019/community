package com.hzz.dao;

import com.hzz.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Bosco
 * @date 2022/2/21
 */

@Mapper
@Repository
public interface DiscussPostMapper {

    /**
     * @param userId 用户id
     * @param offset 起始行行号
     * @param limit 一页显示多少条数据
     * @return 帖子
     */
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    /**
     * @param userId 用户id
     * @return 帖子总行数
     */
    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id, int commentCount);
}
