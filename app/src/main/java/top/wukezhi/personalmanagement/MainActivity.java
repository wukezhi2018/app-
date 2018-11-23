package top.wukezhi.personalmanagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobQueryResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SQLQueryListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import top.wukezhi.personalmanagement.util.HttpUtil;

import static top.wukezhi.personalmanagement.util.key.appkey;

public class MainActivity extends AppCompatActivity {
    private ImageView bingPicImg;//背景图片
    private EditText username;//邮箱
    private EditText password;//密码
    private Button loginButton;//登录按钮
    private TextView resetPassword;//忘记密码
    private TextView register;//注册
    public static final String EXIT_APP = "exit_app";//退出标志
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            //系统版本号判断只有5.0及以上系统才会进入
            View decorView = getWindow().getDecorView();//拿到当前活动的DecorView
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            //setSystemUiVisibility()方法改变系统的UI显示，参数表示活动的布局会显示在状态栏上面，使得背景图与状态栏融合在一起
            //但是背景图和状态栏会紧贴借助fitsSystemWindows="true"解决在weather布局中的线性布局加入
            getWindow().setStatusBarColor(Color.TRANSPARENT);//状态栏设置为透明
        }
        setContentView(R.layout.activity_main);
        Bmob.initialize(MainActivity.this,appkey);
        BmobUser bmobUser=BmobUser.getCurrentUser();
        if(bmobUser!=null){
            Intent intent=new Intent(MainActivity.this,WelcomeActivity.class);
            intent.putExtra("username",bmobUser.getUsername());
            startActivity(intent);
        }
        bingPicImg=(ImageView)findViewById(R.id.login_img);
        loadBingPic();
        register=(TextView)findViewById(R.id.register);
        resetPassword=(TextView)findViewById(R.id.resetPassword);
        loginButton=(Button)findViewById(R.id.login_bt);
        username=(EditText)findViewById(R.id.login_username);
        password=(EditText)findViewById(R.id.login_password);
        //注册操作
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });
        //忘记密码
        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,ResetPassword.class);
                startActivity(intent);
            }
        });
        //登录事件
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //检查输入
                if(inspect()){
                    checkEmail();//确认是否是验证的邮箱并登录
                }
            }
        });
    }
    public void login(){
        //登录操作
        BmobUser.loginByAccount(username.getText().toString(),
                password.getText().toString(), new LogInListener<BmobUser>() {
                    @Override
                    public void done(BmobUser user, BmobException e) {
                        if (user != null) {
                            Intent intent = new Intent(MainActivity.this,
                                    WelcomeActivity.class);
                            intent.putExtra("username",username.getText().toString());
                            intent.putExtra("password",password.getText().toString());
                            startActivity(intent);//进入welcome界面
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "邮箱或密码错误！", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    //确认邮箱状态
    public void checkEmail(){
        String bql ="select emailVerified from _User where username=?";//查询邮箱的验证状态
        new BmobQuery<BmobUser>().doSQLQuery(bql,new SQLQueryListener<BmobUser>(){
            @Override
            public void done(BmobQueryResult<BmobUser> result, BmobException e) {
                if(e ==null){
                    List<BmobUser> list = (List<BmobUser>) result.getResults();
                    if(list!=null && list.size()>0){
                        for (int i=0;i<list.size();i++){
                            if(list.get(i).getEmailVerified()){
                                login();
                            }
                        }
                    }else{
                        Toast.makeText(MainActivity.this, "该邮箱没有注册并验证", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Log.i("smile", "错误码："+e.getErrorCode()+"，错误描述："+e.getMessage());
                }
            }
        },username.getText().toString());
    }
    //检查输入
    public boolean inspect(){
        if (TextUtils.isEmpty(username.getText())) {
            Toast.makeText(this, "邮箱不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!isEmail(username.getText().toString().trim())) {
            Toast.makeText(this, "邮箱格式不符", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(password.getText())) {
            Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    //判断邮箱格式
    public static boolean isEmail(String str) {
        Pattern pattern = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
        Matcher matcher = pattern.matcher(str);
        boolean b = matcher.matches();
        return b;
    }

    /**
     * 加载必应每日一图
     */
    public void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                /*SharedPreferences.Editor editor =
                        PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();*/
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(MainActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }
    //退出应用
    @Override
    protected void onNewIntent(Intent intent) {
        boolean exit_app = intent.getBooleanExtra(EXIT_APP, false);
        if (exit_app) {
            finish();
        }
    }
}
