package com.whitelabel.app.ui.home.presenter;

import com.whitelabel.app.RxUnitTestTools;
import com.whitelabel.app.data.service.IBaseManager;
import com.whitelabel.app.data.service.ICommodityManager;
import com.whitelabel.app.model.CategoryDetailModel;
import com.whitelabel.app.ui.home.HomeCategoryDetailContract;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import  static org.mockito.Mockito.verify;
import java.util.Observable;

import rx.observers.TestSubscriber;

import static org.junit.Assert.*;

/**
 * Created by Administrator on 2017/7/7.
 */
public class HomeCategoryDetailPresenterImplTest {
    HomeCategoryDetailContract.Presenter presenter;
    @Mock
    ICommodityManager  iCommodityManager;
    @Mock
    IBaseManager iBaseManager;
    @Mock
    HomeCategoryDetailContract.View mView;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        RxUnitTestTools.openRxTools();
        presenter=new HomeCategoryDetailPresenterImpl(iCommodityManager,iBaseManager,mView);
    }
    @Test
    public void getCategoryDetail() throws Exception {
        Mockito.when(iBaseManager.isSign()).thenReturn(false);
        CategoryDetailModel categoryDetailModel=new CategoryDetailModel();
        CategoryDetailModel categoryDetailModel1=new CategoryDetailModel();
        Mockito.when(iCommodityManager.getCategoryDetail(true,"4","")).thenReturn(rx.Observable.just(categoryDetailModel));
        Mockito.when(iCommodityManager.getCategoryDetail(false,"4","")).thenReturn(rx.Observable.just(categoryDetailModel1));
        presenter.getCategoryDetail("4");
//        ArgumentCaptor<String> argumentCaptor=ArgumentCaptor.forClass(String.class);
//        verify(mView).showErrorMsg(argumentCaptor.capture());
//        System.out.print(argumentCaptor.getValue());
        verify(mView).loadData(categoryDetailModel);
        verify(mView).showSwipeLayout();
        verify(mView).closeSwipeLayout();
        verify(mView).loadData(categoryDetailModel1);

    }

}