package com.whitelabel.app.data.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.whitelabel.app.data.model.MergeBatchResponse;
import com.whitelabel.app.data.preference.ICacheApi;
import com.whitelabel.app.data.preference.model.ShoppingItemLocalModel;
import com.whitelabel.app.data.retrofit.ShoppingCartApi;
import com.whitelabel.app.model.ApiException;
import com.whitelabel.app.model.GOUserEntity;
import com.whitelabel.app.model.GuestListResponse;
import com.whitelabel.app.model.ResponseModel;
import com.whitelabel.app.model.ShoppingCartDeleteCellEntity;
import com.whitelabel.app.model.ShoppingCartListEntityCart;
import com.whitelabel.app.model.ShoppingCartListEntityCell;
import com.whitelabel.app.model.ShoppingCartVoucherApplyEntity;
import com.whitelabel.app.utils.LanguageUtils;
import com.whitelabel.app.utils.StoreUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Administrator on 2017/7/12.
 */
public class ShoppingCartManager implements IShoppingCartManager {

    private ShoppingCartApi shoppingCartApi;

    private ICacheApi iCacheApi;

    @Inject
    public ShoppingCartManager(ShoppingCartApi shoppingCartApi, ICacheApi iCacheApi) {
        this.shoppingCartApi = shoppingCartApi;
        this.iCacheApi = iCacheApi;
    }

    @Override
    public Observable<ShoppingCartListEntityCart> getShoppingCartInfo(String sessionKey) {
        return shoppingCartApi.getShoppingCartInfo(sessionKey).
            flatMap(new Func1<JsonObject, Observable<ShoppingCartListEntityCart>>() {
                @Override
                public Observable<ShoppingCartListEntityCart> call(JsonObject jsonObject) {
                    int status = jsonObject.get("status").getAsInt();
                    if (status == 1) {
                        JsonObject jsonObject1 = jsonObject.getAsJsonObject("cart");
                        ShoppingCartListEntityCart shoppingCartListEntityCart =
                            new Gson().fromJson(jsonObject1.toString(),
                                ShoppingCartListEntityCart.class);
                        //when
                        if (shoppingCartListEntityCart != null) {
                            for (ShoppingCartListEntityCell cell : shoppingCartListEntityCart
                                .getItems()) {
                                if (cell.getQty() != null) {
                                    cell.setCurrStockQty(
                                        Integer.parseInt(cell.getQty()) + cell.getStockQty() + "");
                                }
                            }
                        }
                        return Observable.just(shoppingCartListEntityCart);
                    } else {
                        String errorMessage = jsonObject.get("errorMessage").getAsString();
                        return Observable.error(new ApiException(errorMessage));
                    }
                }
            }).doOnNext(new Action1<ShoppingCartListEntityCart>() {
            @Override
            public void call(ShoppingCartListEntityCart shoppingCartListEntityCart) {
                GOUserEntity goUserEntity = iCacheApi.getUser();
                goUserEntity.setCartItemCount(shoppingCartListEntityCart.getSummaryQty());
                iCacheApi.saveUser(goUserEntity);
            }
        });
    }

    @Override
    public int getProductCountFromLocal() {
        return iCacheApi.getShoppingListSizeFromLocal();
    }

    @Override
    public Observable<List<ShoppingItemLocalModel>> getProductListFromLocal() {
        return iCacheApi.getShoppingListFromLocal();
    }

    @Override
    public Observable<GuestListResponse> getGuestList(
        List<ShoppingItemLocalModel> shoppingItemLocalModels) {
        Map<String, String> params = new HashMap<>();
        String languageCode = LanguageUtils.getCurrentLanguageCode();
        String storeId = StoreUtils.getStoreIdByLanguageCode(languageCode
                                                            .equalsIgnoreCase(LanguageUtils.LANGUAGE_CODE_AUTO)
                                                            ? LanguageUtils.LANGUAGE_CODE_ENGLISH
                                                            : languageCode);
        params.put("store_id", storeId);
        for (int i = 0; i < shoppingItemLocalModels.size(); i++) {
            params.put("products[" + i + "][group_id]", shoppingItemLocalModels.get(i).getGroupId());
            params.put("products[" + i + "][qty]", shoppingItemLocalModels.get(i).getNumber());
            params.put("products[" + i + "][simple_id]",
                shoppingItemLocalModels.get(i).getSimpleId());
        }
        return shoppingCartApi.getGuestList(params);
    }

    @Override
    public Observable<Boolean> deleteLocalShoppingItem(String simpleId) {
        return iCacheApi.deleteShoppingItem(simpleId);
    }

    @Override
    public Observable<Boolean> updateLocalShoppingItemNumber(String simpleId, String s) {
        return iCacheApi.updateLocalShoppingItemNumber(simpleId, s);
    }

    @Override
    public Observable<Boolean> saveProductToLocal(
        List<ShoppingItemLocalModel> shoppingItemLocalModels) {
        return iCacheApi.addProductDetailToLocal(shoppingItemLocalModels);
    }

    @Override
    public Observable<MergeBatchResponse> addBatchShopping(
        List<ShoppingItemLocalModel> shoppingItemLocalModels, String session) {
        Map<String, String> params = new HashMap<>();
        params.put("session_key", session);
        for (int i = 0; i < shoppingItemLocalModels.size(); i++) {
            params.put("products[" + i + "][main_id]", shoppingItemLocalModels.get(i).getGroupId());
            params.put("products[" + i + "][qty]", shoppingItemLocalModels.get(i).getNumber());
            params.put("products[" + i + "][simple_id]", shoppingItemLocalModels.get(i).getSimpleId());
        }
        return shoppingCartApi.addBatchShopping(params).flatMap(
            new Func1<MergeBatchResponse, Observable<MergeBatchResponse>>() {
                @Override
                public Observable<MergeBatchResponse> call(MergeBatchResponse mergeBatchResponse) {
                    if (mergeBatchResponse.getStatus() == 1) {
                        return Observable.just(mergeBatchResponse);
                    } else {
                        return Observable.error(new ApiException(""));
                    }

                }
            });
    }

    @Override
    public Observable<Boolean> clearShoppingItem() {
        return iCacheApi.clearShoppingItem();
    }

    @Override
    public Observable<ResponseModel> addProductToShoppingCart(String sessionKey, String productId,
        Map<String, String> idQtys) {
        Map<String, String> params = new HashMap<>();
        int index = 0;
        for (String id : idQtys.keySet()) {
            params.put("simpleId[" + index + "]", id);
            params.put("qty[" + index + "]", idQtys.get(id));
            index++;
        }
        return shoppingCartApi.addProductToShoppingCart(sessionKey, productId, params)
            .flatMap(new Func1<ResponseModel, Observable<ResponseModel>>() {
                @Override
                public Observable<ResponseModel> call(ResponseModel responseModel) {
                    if (responseModel.getStatus() == -1) {
                        return Observable.error(new ApiException(responseModel.getErrorMessage()));
                    } else {
                        return Observable.just(responseModel);
                    }
                }
            });
    }

    @Override
    public Observable<ShoppingCartVoucherApplyEntity> applyOrCancelVercherCode(String sessionKey,
        String vercherCode, String state) {
        return shoppingCartApi.applyOrCancelVoucherCode(sessionKey, vercherCode, state);
    }

    @Override
    public Observable<ResponseModel> checkoutOfStock(String sessionKey) {
        return shoppingCartApi.checkoutOutOfStock(sessionKey);
    }

    @Override
    public Observable<ShoppingCartDeleteCellEntity> deleteProductFromShoppingCart(String sessionKey,
        String id) {
        return shoppingCartApi.deleteProductFromShoppingCart(sessionKey, id);
    }

    @Override
    public Observable<ShoppingCartDeleteCellEntity> setProductCountFromShoppingCart(
        String sessionKey, String productId, int count) {
        Map<String, String> params = new HashMap<>();
        params.put("cart[" + productId + "][qty]", count + "");
        return shoppingCartApi.setProductCountFromShoppingCart(sessionKey, params);
    }


}
