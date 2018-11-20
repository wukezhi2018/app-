package top.wukezhi.personalmanagement;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import cn.bmob.v3.Bmob;

import static top.wukezhi.personalmanagement.util.key.appkey;

public class WelcomeActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private String username;
    private String password;
    private TextView name;
    private Button title_bt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        date();//获得传递的数据
        Bmob.initialize(this,appkey);
        //滑动页面点击效果
        drawerLayout=(DrawerLayout)findViewById(R.id.welcome_drawer_layout);
        title_bt=(Button)findViewById(R.id.welcom_title_button);
        title_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        //活动页面选项
        NavigationView navView=(NavigationView)findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                drawerLayout.closeDrawers();
                return true;
            }
        });
        //获取滑动控件，并传入数据
        View v=navView.getHeaderView(0);
        name=(TextView)v.findViewById(R.id.nav_header_username);
        name.setText(username);
    }
    //获得传递的数据
    public void date(){
        Intent intent=getIntent();
        username=intent.getStringExtra("username");
        password=intent.getStringExtra("password");
    }
}
