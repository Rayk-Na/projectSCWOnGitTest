package com.offcn.project.service.impl;

import com.alibaba.fastjson.JSON;
import com.netflix.discovery.converters.Auto;
import com.offcn.dycommon.enums.ProjectStatusEnume;
import com.offcn.project.contants.ProjectConstant;
import com.offcn.project.enums.ProjectImageTypeEnume;
import com.offcn.project.mapper.*;
import com.offcn.project.po.*;
import com.offcn.project.service.ProjectCreateService;
import com.offcn.project.vo.req.ProjectBaseInfoVo;
import com.offcn.project.vo.req.ProjectRedisStorageVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import springfox.documentation.spring.web.json.Json;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
@Service
public class ProjectCreateServiceImpl implements ProjectCreateService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private TProjectMapper projectMapper;

    @Autowired
    private TProjectImagesMapper projectImagesMapper;

    @Autowired
    private TProjectTagMapper projectTagMapper;

    @Autowired
    private TProjectTypeMapper projectTypeMapper;

    @Autowired
    private TReturnMapper tReturnMapper;

    @Override
    public String initCreateProject(Integer memberId) {
        String token = UUID.randomUUID().toString().replace("-", "");
        //项目的临时对象
        ProjectRedisStorageVo initVo = new ProjectRedisStorageVo();
        initVo.setMemberid(memberId);
        //initVo转为json字符串
        String jsonString = JSON.toJSONString(initVo);
        //TEMP_PROJECT_PREFIX = "project:create:temp:";
        stringRedisTemplate.opsForValue().set(ProjectConstant.TEMP_PROJECT_PREFIX + token, jsonString);

        return token;
    }

    @Override
    public void saveProjectInfo(ProjectStatusEnume enume, ProjectRedisStorageVo redisStorageVo) {

        //1、添加项目表
        TProject project = new TProject();
        BeanUtils.copyProperties(redisStorageVo,project);
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = simpleDateFormat.format(date);
        project.setCreatedate(time);
        projectMapper.insert(project);

        //2、获取刚刚新增项目id  作为后面图片 和 类型 标签等的外键
        Integer pid = project.getId();
        //3、存图片
        //3.1 存头部图片
        TProjectImages images = new TProjectImages(null,pid,redisStorageVo.getHeaderImage(), ProjectImageTypeEnume.HEADER.getCode());
        projectImagesMapper.insert(images);
        //3.2 存详细图片
        List<String> detailsImages = redisStorageVo.getDetailsImage();
        for (String detailsImage : detailsImages) {
            TProjectImages detailsIMG = new TProjectImages(null,pid,detailsImage,ProjectImageTypeEnume.DETAILS.getCode());
            projectImagesMapper.insert(detailsIMG);
        }
        //4、存tag
        List<Integer> tagList = redisStorageVo.getTagids();
        for (Integer tid : tagList) {
            TProjectTag projectTag = new TProjectTag(null,pid,tid);
            projectTagMapper.insert(projectTag);
        }

        //5、存type
        List<Integer> typeList = redisStorageVo.getTypeids();
        for (Integer tyId : typeList) {
            TProjectType tt = new TProjectType(null,pid,tyId);
            projectTypeMapper.insert(tt);
        }

        //6、存return
        List<TReturn> returnList = redisStorageVo.getProjectReturns();
        for (TReturn tReturn : returnList) {
            tReturn.setProjectid(pid);
            tReturnMapper.insert(tReturn);
        }

        //7、移除redis对应的project
        stringRedisTemplate.delete(ProjectConstant.TEMP_PROJECT_PREFIX+redisStorageVo.getProjectToken());
    }
}
