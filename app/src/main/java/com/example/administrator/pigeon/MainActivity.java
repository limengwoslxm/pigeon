package com.example.administrator.pigeon;

import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.xys.libzxing.zxing.activity.CaptureActivity;

import java.util.ArrayList;

import adapter.ConversationListAdapterEx;
import bean.User;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import fragment.ChatFragment;
import fragment.SelfInfoFragment;
import fragment.ZoneFragment;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;
import listener.OnFragmentResultListener;
import model.UserModel;
import myapp.MyApp;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, RadioGroup.OnCheckedChangeListener {

    @ViewInject(R.id.rb_chat)
    RadioButton rb_chat;
    @ViewInject(R.id.rb_conver)
    RadioButton rb_conver;
    @ViewInject(R.id.rb_zone)
    RadioButton rb_zone;
    @ViewInject(R.id.rb_user)
    RadioButton rb_user;
    @ViewInject(R.id.rg_tab)
    RadioGroup radioGroup;
    @ViewInject(R.id.viewpager)
    ViewPager viewPager;

    RadioButton[] rbs;
    ArrayList<Fragment> fragments;
    Fragment mConversationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        ViewUtils.inject(this);
        rbs = new RadioButton[]{rb_chat,rb_conver,rb_zone,rb_user};
        for (RadioButton rb : rbs) {
            //挨着给每个RadioButton加入drawable限制边距以控制显示大小
            Drawable drs[] = rb.getCompoundDrawables();
            //获取drawables
            Rect r = new Rect(0, 0, drs[1].getMinimumWidth() * 1 / 2, drs[1].getMinimumHeight() * 1 / 2);
            //定义一个Rect边界
            drs[1].setBounds(r);
            //给drawable设置边界
            rb.setCompoundDrawables(null, drs[1], null, null);
        }
        radioGroup.setOnCheckedChangeListener(this);

        fragments = new ArrayList<>();
        mConversationList = initConversationList();
        fragments.add(new ChatFragment());
        fragments.add(mConversationList);
        fragments.add(new ZoneFragment());
        fragments.add(new SelfInfoFragment());

        FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()){
            @Override
            public Fragment getItem(int i) {
                return fragments.get(i);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }
        };
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(this);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("信鸽");
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_chat:
                setItem(0);
                break;
            case R.id.rb_conver:
                setItem(1);
                break;
            case R.id.rb_zone:
                setItem(2);
                break;
            case R.id.rb_user:
                setItem(3);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int i) {
//        Toast.makeText(this,i+"",Toast.LENGTH_SHORT).show();
        if(!rbs[i].isChecked()){
            rbs[i].setChecked(true);
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    public void setItem(int i){
        if(viewPager.getCurrentItem() != i){
            viewPager.setCurrentItem(i);
        }
    }

    private Fragment initConversationList(){
        if(mConversationList == null){
            ConversationListFragment listFragment = new ConversationListFragment();
            listFragment.setAdapter(new ConversationListAdapterEx(RongContext.getInstance()));
            Uri uri;
            uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                    .appendPath("conversationlist")
                    .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "false") //设置私聊会话是否聚合显示
                    .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "true")//群组
                    .appendQueryParameter(Conversation.ConversationType.DISCUSSION.getName(),"true")//讨论组
                    .appendQueryParameter(Conversation.ConversationType.PUBLIC_SERVICE.getName(), "false")//公共服务号
                    .appendQueryParameter(Conversation.ConversationType.APP_PUBLIC_SERVICE.getName(), "false")//订阅号
                    .appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "true")//系统
                    .build();
            listFragment.setUri(uri);
            return listFragment;
        }else{
            return mConversationList;
        }
    }

    public void onBackPressed() {
        //方式一：将此任务转向后台
//        moveTaskToBack(true);

//        方式二：返回手机的主屏幕
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if(keyCode == KeyEvent.KEYCODE_BACK){
//            moveTaskToBack(true);
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_search:
                startActivity(new Intent(this,SearchActivity.class));
                break;
            case R.id.action_add:
                startActivityForResult(new Intent(this,CaptureActivity.class),1);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //得到fragment
        Fragment fragment = fragments.get(fragments.size()-1);
        /**
        * 判断是不是Activity里面Fragment的回调，如果是传递给Fragment
        */
        Log.i("onActivityResult","requestCode="+requestCode+"resultCode="+resultCode+"");
        if (requestCode == 66) {
            OnFragmentResultListener listener = (OnFragmentResultListener) fragment;
            listener.OnFragmentResult(requestCode, resultCode, data);
        }else {
            if (data != null){
                final String result = data.getExtras().getString("result");
                Log.i("result",result);
                UserModel.getInstance(this).toDetails(result);
            }

        }

    }
}
