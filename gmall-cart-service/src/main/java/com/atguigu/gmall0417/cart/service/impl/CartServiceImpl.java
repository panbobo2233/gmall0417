package com.atguigu.gmall0417.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall0417.bean.CartInfo;
import com.atguigu.gmall0417.bean.SkuInfo;
import com.atguigu.gmall0417.cart.constant.CartConst;
import com.atguigu.gmall0417.cart.mapper.CartInfoMapper;
import com.atguigu.gmall0417.config.RedisUtil;
import com.atguigu.gmall0417.service.CartService;
import com.atguigu.gmall0417.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Reference
    private ManageService manageService;

    @Autowired
    private RedisUtil redisUtil;
    @Override
    public void addToCart(String skuId, String userId, Integer skuNum) {
        //先判断购物车中是否有该商品 --skuid取得数据
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        cartInfo.setSkuId(skuId);



        //查询一下:select * from cart_info where skuid=? and userid=?
        CartInfo cartInfoExist = cartInfoMapper.selectOne(cartInfo);
        //如果有商品 数量+1
        if (cartInfoExist!=null){
            //数量+skuNum
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum()+skuNum);
            //更新数据库
            cartInfoMapper.updateByPrimaryKey(cartInfoExist);
        }else {
            //没有商品，新增
            //cartinfo 所有信息来自skuinfo
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);

            CartInfo cartInfo1 = new CartInfo();
            cartInfo1.setSkuId(skuId);
            cartInfo1.setCartPrice(skuInfo.getPrice());
            cartInfo1.setSkuPrice(skuInfo.getPrice());
            cartInfo1.setSkuName(skuInfo.getSkuName());
            cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo1.setUserId(userId);
            cartInfo1.setSkuNum(skuNum);
            //放入数据库
            cartInfoMapper.insertSelective(cartInfo1);
            cartInfoExist = cartInfo1;
        }
        //放入缓存
        Jedis jedis = redisUtil.getJedis();
        //放入数据
        //定义key user:userId:cart
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        //hset(key,field,value) key: user:userId:cart field:skuId value:cartInfo
        jedis.hset(userCartKey,skuId, JSON.toJSONString(cartInfoExist));

        //过期时间：根据用户的过期时间来设置
        //redis 怎么获取key的过期时间ttl（key）
        String userInfoKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USERINFOKEY_SUFFIX;
        Long ttl = jedis.ttl(userInfoKey);
        jedis.expire(userCartKey,ttl.intValue());
        jedis.close();
    }

    @Override
    public List<CartInfo> getCartList(String userId) {

        //1.从redis取
        Jedis jedis = redisUtil.getJedis();
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        //key:user:userId:cart
        //String cartJson = jedis.hget(userCartKey,);
        //hash取值方式，将hash中的所有值一次性全部出去哪个方法？
        List<String> cartJsons = jedis.hvals(userCartKey);
        List<CartInfo> cartInfoList = new ArrayList<>();
        //判断遍历
        if (cartJsons!=null&& cartJsons.size()>0){
            for (String cartJson:cartJsons){
                //cartJson对应的是每一个skuId的值，将cartJson转换成我们的cartinfo对象
                CartInfo cartInfo = JSON.parseObject(cartJson,CartInfo.class);
                cartInfoList.add(cartInfo);
            }
            //做一个排序
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    //string类常用6 length() equals() indexOf()  trim() compareTo()比较大小
                    return o1.getId().compareTo(o2.getId());
                }
            });
            return cartInfoList;
        }else{
            //缓存中没有数据---db
            List<CartInfo> cartInfoListDB = loadCartCache(userId);
            return cartInfoListDB;
        }
    }

    private List<CartInfo> loadCartCache(String userId) {
        //根据userid查询cartinfo商品信息，然后记得放入缓存
        //但是可能出现商品价格不一致的情况
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);
        if (cartInfoList==null && cartInfoList.size()==0){
            return null;
        }

        //将其放入redis
        Jedis jedis = redisUtil.getJedis();
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;

        //迭代：hset(key,field,value)： 把field，value看成一个map
        // field：key  value: 单个cartinfo的字符串:v
        Map<String,String> map = new HashMap<>(cartInfoList.size());
        for (CartInfo cartInfo:cartInfoList){
            //将其对象变成字符串
            String newCartInfo = JSON.toJSONString(cartInfo);
            map.put(cartInfo.getSkuId(),newCartInfo);
        }
        //hmset表示一次存入多个值
        jedis.hmset(userCartKey,map);
        jedis.close();
        return cartInfoList;
    }

    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartListFromCookie, String userId) {
        //获取redis中的数据
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);

        //cartListFromCookie 循环匹配[skuId]
        //skuid相等就是有同样的产品数量相加
        for (CartInfo cartInfoCK:cartListFromCookie){
            boolean isMatch = false;
            for (CartInfo cartInfoDB:cartInfoList){
                if (cartInfoCK.getSkuId().equals(cartInfoDB.getSkuId())){
                    cartInfoDB.setSkuNum(cartInfoDB.getSkuNum()+cartInfoCK.getSkuNum());
                    //更新数据库
                    cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);
                    isMatch=true;
                }
            }
            //不相等的情况
            //说明当前数据库中没有cookie的商品
            if (!isMatch){
                cartInfoCK.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfoCK);
            }
        }
        //以上代码只操作数据库，将数据库变为最新的数据
        //以下代码是根据当前的用户重新查询最新数据并同步redis
        //同步redis
        List<CartInfo> infoList = loadCartCache(userId);
        //有没有判读ischecked?
        for (CartInfo cartInfoDB:infoList){
            //循环cookie
            for (CartInfo infoCK:cartListFromCookie){
                //有相同的产品
                if (cartInfoDB.getSkuId().equals(infoCK.getSkuId())){
                    //判断cookie中的商品是否选中，如果有选中的，更新数据库
                    if ("1".equals(infoCK.getIsChecked())){
                        //重新给db对象赋值为选中状态
                        cartInfoDB.setIsChecked("1");
                        //重新调用一下checkcart
                        checkCart(cartInfoDB.getSkuId(),infoCK.getIsChecked(),userId);
                    }
                }
            }
        }



        return infoList;
    }

    @Override
    public void checkCart(String skuId, String isChecked, String userId) {
        //改变之前所有购物车中的商品的状态
        //准备redis key
        Jedis jedis = redisUtil.getJedis();
        //user:userId:cart
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        //将redis中的数据取出来rhset（key，field，value） hget（key，field）
        String cartJson = jedis.hget(userCartKey, skuId);
        //将redis的字符串转换成对象
        CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
        if (cartInfo!=null){
            cartInfo.setIsChecked(isChecked);
        }
        //将修改完成的对象重新给redis
        jedis.hset(userCartKey,skuId,JSON.toJSONString(cartInfo));
        //新创建一个key用来存储被选中的商品
        String userCheckedKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;
        if ("1".equals(isChecked)){
            //选中则添加
            jedis.hset(userCheckedKey,skuId,JSON.toJSONString(cartInfo));
        }else {
            jedis.hdel(userCheckedKey,skuId);
        }
        jedis.close();


    }
}
