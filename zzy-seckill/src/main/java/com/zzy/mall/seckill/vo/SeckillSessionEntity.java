package com.zzy.mall.seckill.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class SeckillSessionEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long id;

	private String name;

	private Date startTime;

	private Date endTime;

	private Integer status;

	private Date createTime;

	private List<SeckillSkuRelationEntity> relationEntities;

}
