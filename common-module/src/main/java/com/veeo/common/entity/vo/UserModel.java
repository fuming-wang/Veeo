package com.veeo.common.entity.vo;

import com.veeo.common.holder.UserHolder;
import lombok.Data;


import java.util.ArrayList;
import java.util.List;


@Data
public class UserModel {

    private List<Model> models;
    private Long userId;

    public static UserModel buildUserModel(List<String> labels, Long videoId, Double score){
        UserModel userModel = new UserModel();
        List<Model> models = new ArrayList<>();
        userModel.setUserId(UserHolder.get());
        labels.forEach(label -> {
            Model model = new Model();
            model.setLabels(label);
            model.setScore(score);
            model.setId(videoId);
            models.add(model);
        });
        userModel.setModels(models);
        return userModel;
    }

}
