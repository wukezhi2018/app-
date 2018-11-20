package top.wukezhi.personalmanagement;

import android.provider.ContactsContract;
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
import cn.bmob.v3.listener.SQLQueryListener;
import cn.bmob.v3.listener.UpdateListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import top.wukezhi.personalmanagement.util.HttpUtil;

import static top.wukezhi.personalmanagement.util.key.appkey;

public class ResetPassword extends AppCompatActivity {
    private EditText username;
    private Button send;
    private Button back;
    private ImageView bingPicImg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        Bmob.initialize(this,appkey);

        back=(Button)findViewById(R.id.resetpassword_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        bingPicImg=(ImageView)findViewById(R.id.reset_img);
        loadBingPic();

        send=(Button)findViewById(R.id.reset_bt);
        username=(EditText)findViewById(R.id.reset_username);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inspect()){
                    lookUser();
                }
            }
        });
    }
    //判断有无此用户
    public void lookUser(){
        String bql ="select username from _User where username=?";//查询邮箱的验证状态
        new BmobQuery<BmobUser>().doSQLQuery(bql,new SQLQueryListener<BmobUser>(){
            @Override
            public void done(BmobQueryResult<BmobUser> result, BmobException e) {
                if(e ==null){
                    List<BmobUser> list = (List<BmobUser>) result.getResults();
                    if(list!=null && list.size()>0){
                        sendEmail();
                    }else{
                        Toast.makeText(ResetPassword.this, "该邮箱没有注册并验证", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Log.i("smile", "错误码："+e.getErrorCode()+"，错误描述："+e.getMessage());
                }
            }
        },username.getText().toString());
    }
    //发送邮件重置密码
    public void sendEmail(){
        //重置密码
        final String email = username.getText().toString();
        BmobUser.resetPasswordByEmail(email, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if(e==null){
                    Toast.makeText(ResetPassword.this,
                            "重置密码请求成功，请到" + email + "邮箱进行密码重置操作",Toast.LENGTH_SHORT).show();
                    finish();
                }else{
                    Toast.makeText(ResetPassword.this,
                            "失败:" + e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    //验证输入
    public boolean inspect(){
        if (TextUtils.isEmpty(username.getText())) {
            Toast.makeText(this, "邮箱不能为空", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!isEmail(username.getText().toString().trim())) {
            Toast.makeText(this, "邮箱格式不符", Toast.LENGTH_SHORT).show();
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
                        Glide.with(ResetPassword.this).load(bingPic).into(bingPicImg);
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
