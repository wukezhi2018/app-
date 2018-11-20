package top.wukezhi.personalmanagement;

import android.os.CountDownTimer;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import top.wukezhi.personalmanagement.util.HttpUtil;


import static top.wukezhi.personalmanagement.util.key.appkey;

public class RegisterActivity extends AppCompatActivity {
    private ImageView bingPicImg;
    private EditText password;
    private EditText password1;
    private EditText username;
    private Button register_bt;
    private Button register_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Bmob.initialize(RegisterActivity.this,appkey);

        bingPicImg=(ImageView)findViewById(R.id.register_img);
        loadBingPic();

        username=(EditText)findViewById(R.id.register_username);
        password=(EditText)findViewById(R.id.register_password);
        password1=(EditText)findViewById(R.id.register_password1);
        register_bt=(Button)findViewById(R.id.register_bt);
        register_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });

        register_button=(Button)findViewById(R.id.register_button);
        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    public void register(){
        if(inspect()){
            final BmobUser bmobUser=new BmobUser();
            bmobUser.setUsername(username.getText().toString());
            bmobUser.setPassword(password.getText().toString());
            bmobUser.setEmail(username.getText().toString());
            bmobUser.signUp(new SaveListener<BmobUser>() {
                @Override
                public void done(BmobUser bmobUser1, BmobException e){
                    if(e==null){
                        inspectEmail();//验证邮箱
                    }else{
                        Toast.makeText(RegisterActivity.this,
                                "已经存在邮箱！",Toast.LENGTH_LONG).show();
                    }
                }
            });


        }
    }
    //验证邮箱，调用强制重新发送
    public void inspectEmail(){
        final String email =username.getText().toString();
        BmobUser.requestEmailVerify(email, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if(e==null){
                    Toast.makeText(RegisterActivity.this,
                            "请求验证邮件成功，请到" + email + "邮箱中进行激活。",Toast.LENGTH_LONG).show();
                    finish();
                }else{
                    Toast.makeText(RegisterActivity.this,
                            "注册不成功邮箱不存在！",Toast.LENGTH_LONG).show();
                }
            }
        });
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
        if (TextUtils.isEmpty(password1.getText())) {
            Toast.makeText(this, "确认密码不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!TextUtils.equals(password.getText(), password1.getText())) {
            Toast.makeText(this, "两次密码不一致", Toast.LENGTH_SHORT).show();
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(RegisterActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }
}
