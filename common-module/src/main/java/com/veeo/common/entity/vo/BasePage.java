package com.veeo.common.entity.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.veeo.common.entity.user.Follow;
import com.veeo.common.entity.user.Role;
import com.veeo.common.entity.user.User;
import com.veeo.common.entity.video.Type;
import com.veeo.common.entity.video.Video;
import lombok.Data;


@Data
public class BasePage {

    private Long page = 1L;
    private Long limit = 15L;

    public IPage<User> userPage(){
        return new Page<>(page == null ? 1L : this.page, limit == null ? 15L : this.limit);
    }

    public IPage<Type> typePage(){
        return new Page<>(page == null ? 1L : this.page, limit == null ? 15L : this.limit);
    }

    public IPage<Video> videoPage(){
        return new Page<>(page == null ? 1L : this.page, limit == null ? 15L : this.limit);
    }

    public IPage<Role> rolePage(){
        return new Page<>(page == null ? 1L : this.page, limit == null ? 15L : this.limit);
    }


    public IPage<Follow> followPage(){
        return new Page<>(page == null ? 1L : this.page, limit == null ? 15L : this.limit);
    }

    public IPage page(){
        return new Page(page == null ? 1L : this.page, limit == null ? 15L : this.limit);
    }
}
