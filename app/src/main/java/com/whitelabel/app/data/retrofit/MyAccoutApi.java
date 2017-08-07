package com.whitelabel.app.data.retrofit;

import com.whitelabel.app.model.AddToWishlistEntity;
import com.whitelabel.app.model.AddresslistReslut;
import com.whitelabel.app.model.NotificationUnReadResponse;
import com.whitelabel.app.model.ResponseModel;
import com.whitelabel.app.model.SVRAppserviceCatalogSearchReturnEntity;
import com.whitelabel.app.model.WishDelEntityResult;

import java.util.Observable;

import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Administrator on 2017/6/12.
 */

public interface MyAccoutApi {
    @GET("appservice/customer/customerAddressInfo")
    public rx.Observable<AddresslistReslut>  getAddressList(@Query("session_key") String sessionKey);
    @GET("appservice/customer/removeAddress")
    public  rx.Observable<ResponseModel> deleteAddressById(@Query("session_key") String sessionKey,@Query("address_id") String addressId);

    @FormUrlEncoded
    @POST("appservice/wishlist/remove")
    public rx.Observable<WishDelEntityResult> deleteWishById(@Field("session_key")String sessionKey, @Field("item_id") String itemId);

    @FormUrlEncoded
    @POST("appservice/wishlist/addToWishList")
    public rx.Observable<AddToWishlistEntity>  addWish(@Field("session_key")String sessionKey, @Field("product_id") String productId);

    @GET("appservice/notification/unreads/{user_id}")
    public rx.Observable<ResponseModel<NotificationUnReadResponse>>  getNotificationUnReadResponse(@Path("user_id") String userId);

}
