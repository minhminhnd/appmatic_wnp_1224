package com.whitelabel.app.data.service;

import com.whitelabel.app.data.preference.PreferHelper;
import com.whitelabel.app.data.retrofit.ProductApi;
import com.whitelabel.app.model.AddressBook;
import com.whitelabel.app.model.CategoryDetailModel;
import com.whitelabel.app.model.ProductDetailModel;
import com.whitelabel.app.model.ResponseModel;
import com.whitelabel.app.model.SVRAppserviceCatalogSearchReturnEntity;
import com.whitelabel.app.model.SVRAppserviceProductDetailReturnEntity;
import com.whitelabel.app.model.ShoppingCartListEntityCell;
import com.whitelabel.app.model.TMPLocalCartRepositoryProductEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Administrator on 2017/7/5.
 */
public class CommodityManager  implements ICommodityManager{
    private ProductApi  productApi;
    private PreferHelper  cacheHelper;
    public SVRAppserviceCatalogSearchReturnEntity svrAppserviceCatalogSearchReturnEntity;
    public CommodityManager(ProductApi productApi, PreferHelper preferHelper){
        this.productApi=productApi;
        cacheHelper=preferHelper;
    }
    @Override
    public Observable<SVRAppserviceCatalogSearchReturnEntity> getAllCategoryManager() {
        if(svrAppserviceCatalogSearchReturnEntity==null) {
            return productApi.getBaseCategory()
                    .doOnNext(new Action1<SVRAppserviceCatalogSearchReturnEntity>() {
                @Override
                public void call(SVRAppserviceCatalogSearchReturnEntity svrAppserviceCatalogSearchReturnEntity) {
                    CommodityManager.this.svrAppserviceCatalogSearchReturnEntity = svrAppserviceCatalogSearchReturnEntity;
                }
            });
        }else{
            return Observable.just(svrAppserviceCatalogSearchReturnEntity);
        }
    }
    @Override
    public Observable<Integer> getLocalShoppingProductCount() {
        return cacheHelper.getShoppingCartProduct()
        .flatMap(new Func1<List<TMPLocalCartRepositoryProductEntity>, Observable<Integer>>() {
            @Override
            public Observable<Integer> call(List<TMPLocalCartRepositoryProductEntity> tmpLocalCartRepositoryProductEntities) {
                if(tmpLocalCartRepositoryProductEntities!=null){
                    return Observable.just(sumProductCount(tmpLocalCartRepositoryProductEntities));
                }
                return Observable.just(0);
            }
        });
    }
    public int sumProductCount(List<TMPLocalCartRepositoryProductEntity>  tmps){
        int count=0;
        for(TMPLocalCartRepositoryProductEntity  tmp: tmps){
                count+=tmp.getSelectedQty();
         }
         return count;
    }
    @Override
    public Observable<List<AddressBook>> getAddressListCache(String userId) {
        return cacheHelper.getAddressListCache(userId);
    }
    @Override
    public Observable<CategoryDetailModel> getCategoryDetail(boolean isCache,String category,String sessionKey) {
        if(isCache){
            return cacheHelper.getCategoryDetail(category);
        }else {
            return productApi.getCategoryDetail(category, sessionKey)
                    .map(new Func1<ResponseModel<CategoryDetailModel>, CategoryDetailModel>() {
                        @Override
                        public CategoryDetailModel call(ResponseModel<CategoryDetailModel> categoryDetailModelResponseModel) {
                            return categoryDetailModelResponseModel.getData();
                        }
                    }).doOnNext(new Action1<CategoryDetailModel>() {
                        @Override
                        public void call(CategoryDetailModel categoryDetailModel) {
                            cacheHelper.saveCategoryDetail(categoryDetailModel);
                        }
             });
        }
    }
    @Override
    public Observable<ProductDetailModel> getProductDetail(String sessionKey, String productId) {
         return productApi.getProductDetail(sessionKey,productId)
                 .map(new Func1<SVRAppserviceProductDetailReturnEntity, ProductDetailModel>() {
             @Override
             public ProductDetailModel call(SVRAppserviceProductDetailReturnEntity svrAppserviceProductDetailReturnEntity) {
                 return svrAppserviceProductDetailReturnEntity.getResult();
             }
         });
    }

    @Override
    public Observable<ResponseModel> addProductToShoppingCart(String sessionKey, String productId, Map<String,String> idQtys){
        Map<String ,String> params=new HashMap<>();
        int  index=0;
        for(String id :idQtys.keySet()){
            params.put("simpleId["+index+"]",id);
            params.put("qty["+index+"]",idQtys.get(id));
            index++;
        }
        return  productApi.addProductToShoppingCart(sessionKey,productId,params);
    }

}
