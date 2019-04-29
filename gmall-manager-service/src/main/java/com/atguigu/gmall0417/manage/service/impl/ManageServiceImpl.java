package com.atguigu.gmall0417.manage.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0417.bean.*;
import com.atguigu.gmall0417.config.RedisUtil;
import com.atguigu.gmall0417.manage.constant.ManageConst;
import com.atguigu.gmall0417.manage.mapper.*;
import com.atguigu.gmall0417.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    private baseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    private BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;


    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        return baseCatalog2Mapper.select(baseCatalog2);
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        return baseCatalog3Mapper.select(baseCatalog3);
    }

    @Override
    public List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(Long.parseLong(skuInfo.getId()),Long.parseLong(skuInfo.getSpuId()));
    }

    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {
        return skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);
    }

    //将该方法进行升级改造
    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
        //原来是单表查询的,现在改为单表
//        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
//        baseAttrInfo.setCatalog3Id(catalog3Id);
//        return baseAttrInfoMapper.select(baseAttrInfo);
        List<BaseAttrInfo> baseAttrInfoListByCatalog3Id = baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(Long.parseLong(catalog3Id));
        return baseAttrInfoListByCatalog3Id;
    }

    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //如果有主键就进行更新，如果没有就插入
        if (baseAttrInfo.getId() != null && baseAttrInfo.getId().length() > 0) {
            baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
        } else {
            //防止主键被赋上一个空字符串,改为null才能自增
            if (baseAttrInfo.getId().length() == 0) {
                baseAttrInfo.setId(null);
            }
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }
        //把原属性值全部清空
        BaseAttrValue baseAttrValue4Del = new BaseAttrValue();
        //AttrId = baseAttrInfo.Id
        baseAttrValue4Del.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValue4Del);

        //重新插入属性值
        if (baseAttrInfo.getAttrValueList() != null && baseAttrInfo.getAttrValueList().size() > 0) {
            for (BaseAttrValue attrValue : baseAttrInfo.getAttrValueList()) {
                //防止主键被赋上一个空字符串
                if (attrValue.getId().length() == 0) {
                    attrValue.setId(null);
                }
                //要取得baseattrinfo的id实际上应该获得数据库自增长的id
                attrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(attrValue);


            }
        }
    }


    @Override
    public BaseAttrInfo getAttrInfo(String attrId) {
        // 创建属性对象
        BaseAttrInfo attrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);
        // 创建属性值对象
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        // 根据attrId字段查询对象
        baseAttrValue.setAttrId(attrInfo.getId());
        List<BaseAttrValue> attrValueList = baseAttrValueMapper.select(baseAttrValue);
        // 给属性对象中的属性值集合赋值
        attrInfo.setAttrValueList(attrValueList);
        // 将属性对象返回
        return attrInfo;
    }


    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo) {
        List<SpuInfo> spuInfoList = spuInfoMapper.select(spuInfo);
        return spuInfoList;
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        //保存主表 通过主键存在判断是修改 还是新增
        if (spuInfo.getId() == null || spuInfo.getId().length() == 0) {
            spuInfo.setId(null);
            spuInfoMapper.insertSelective(spuInfo);
        } else {
            spuInfoMapper.updateByPrimaryKey(spuInfo);
        }

        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuInfo.getId());
        spuImageMapper.delete(spuImage);

        //插入数据在哪？spuinfo.getSpuImageList（）
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (spuImageList != null && spuImageList.size() > 0) {
            for (SpuImage image : spuImageList) {
                //image的id设置为null==""；
                if (image.getId() != null && image.getId().length() == 0) {
                    image.setId(null);
                }

                //image中有spuid
                image.setSpuId(spuInfo.getId());
                //插入数据
                spuImageMapper.insertSelective(image);
            }
        }


        //保存销售属性信息  先删除 再插入
        SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
        spuSaleAttr.setSpuId(spuInfo.getId());
        spuSaleAttrMapper.delete(spuSaleAttr);

        SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
        spuSaleAttrValue.setSpuId(spuInfo.getId());
        spuSaleAttrValueMapper.delete(spuSaleAttrValue);

        //插入数据
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();

        if (spuSaleAttrList != null && spuSaleAttrList.size() > 0) {
            for (SpuSaleAttr saleAttr : spuSaleAttrList) {
                if (saleAttr.getId() != null && saleAttr.getId().length() == 0) {
                    saleAttr.setId(null);
                }
                saleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insertSelective(saleAttr);

                //插入属性值
                List<SpuSaleAttrValue> spuSaleAttrValueList = saleAttr.getSpuSaleAttrValueList();
                for (SpuSaleAttrValue saleAttrValue : spuSaleAttrValueList) {
                    if (saleAttrValue.getId() != null && saleAttrValue.getId().length() == 0) {
                        saleAttrValue.setId(null);
                    }
                    saleAttrValue.setSpuId(spuInfo.getId());
                    spuSaleAttrValueMapper.insertSelective(saleAttrValue);

                }
            }
        }
    }

    @Override
    public List<SpuImage> getSpuImageList(String spuId) {
        //spuid传入对象
        //查询返回一个集合
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        return spuImageMapper.select(spuImage);
    }

    @Override
    public SkuInfo getSkuInfo(String skuId){
        SkuInfo skuInfo = null;

       try{
           //添加redis
           //redis如何存？key= sku:skuId:info  sku:,:info作为一个常量。
           //redis中是否一直将skuinfo信息保存？设置一个过期时间setex
           Jedis jedis = redisUtil.getJedis();

           String skuInfoKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;
           String skuJson = jedis.get(skuInfoKey);

           if (skuJson==null || skuJson.length()==0){
               //没有数据，需要加锁！取出完整数据，还要放入缓存中，下次直接从缓存中取得即可
               System.out.println("没有命中缓存");
               //定义Key user:userid::lock
               String skuLockKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKULOCK_SUFFIX;
               //生成锁
               String lockKey = jedis.set(skuLockKey,"OK","NX","PX",ManageConst.SKULOCK_EXPIRE_PX);
               if ("OK".equals(lockKey)){//如果有锁
                   System.out.println("获取锁！");
                   //从数据库中获取数据
                   skuInfo = getSkuInfoDB(skuId);
                   //将数据放入缓存
                   //将对象转为字符串
                   String skuRedisStr = JSON.toJSONString(skuInfo);
                   jedis.setex(skuInfoKey,ManageConst.SKUKEY_TIMEOUT,skuRedisStr);
                   jedis.close();
                   return skuInfo;
               }else {
                   System.out.println("等待");
                   //等待
                  Thread.sleep(1000);
                  //自旋
                   return getSkuInfo(skuId);
               }
           }else {
               //有数据
               System.out.println("有数据");
               skuInfo = JSON.parseObject(skuJson,SkuInfo.class);
               jedis.close();
               return skuInfo;
           }
       }catch (Exception e){
           e.printStackTrace();
       }
       //走这里说明出现错误，redis没启动或者宕机，只能从数据库取
       return getSkuInfoDB(skuId);
    }

    private SkuInfo getSkuInfoDB(String skuId) {
        //skuid=skuinfo.id skuinfo表中的id是不是主键
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        //skuimag放入skuinfo的imagelist集合中即可
        //select * from skuimage where skuid = skuid?
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(skuImageList);
        //留着要写属性值
        //平台属性
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValue);
        skuInfo.setSkuAttrValueList(skuAttrValueList);

        //销售属性
        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuId);
        List<SkuSaleAttrValue> saleAttrValueList = skuSaleAttrValueMapper.select(skuSaleAttrValue);
        skuInfo.setSkuSaleAttrValueList(saleAttrValueList);
        return skuInfo;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrList(Long.parseLong(spuId));
    }

    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        if(skuInfo.getId()==null||skuInfo.getId().length()==0){
            skuInfo.setId(null);
            skuInfoMapper.insertSelective(skuInfo);
        }else{
            skuInfoMapper.updateByPrimaryKeySelective(skuInfo);
        }

        //删除skuimg
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuInfo.getId());
        skuImageMapper.delete(skuImage);
        //插入
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if(skuImageList!=null && skuImageList.size()>0){
            for(SkuImage image:skuImageList){
                if (image.getId()!=null && image.getId().length()==0){
                    image.setId(null);
                }
                //skuid
                image.setSkuId(skuInfo.getId());
                skuImageMapper.insertSelective(image);
            }
        }
        //销售属性
        //sku_sale_attr_value
        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setId(skuInfo.getId());
        skuSaleAttrValueMapper.delete(skuSaleAttrValue);

        //插入数据
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (skuSaleAttrValueList!=null && skuSaleAttrValueList.size()>0){
            for(SkuSaleAttrValue saleAttrValue:skuSaleAttrValueList){
                if (saleAttrValue.getId()!=null && saleAttrValue.getId().length()==0){
                    saleAttrValue.setId(null);
                }
                //skuid
                saleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValueMapper.insertSelective(saleAttrValue);
            }
        }
//        sku_attr_value
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setId(skuInfo.getId());
        skuAttrValueMapper.delete(skuAttrValue);

        //插入数据
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (skuAttrValueList!=null && skuAttrValueList.size()>0){
            for(SkuAttrValue attrValue:skuAttrValueList){
                if (attrValue.getId()!=null && attrValue.getId().length()==0){
                    attrValue.setId(null);
                }
                //skuid
                attrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insertSelective(attrValue);
            }
        }
    }

    @Override
    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList) {
        //使用工具类StringUtil
        String attrValueIds = StringUtils.join(attrValueIdList.toArray(), ",");


        //调用mapper==attrValueIdList集合，我们需要将集合中的id遍历查出数据
        /**
         * 有两种方法，一种foreach标签，一种select * from baseAttrInfo where id in (1234)
         * 第二种比较好一点,第一种是一个mybatis标签，第二种是sql语句
         */
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.selectAttrInfoListByIds(attrValueIds);
        return baseAttrInfoList;
    }
}





