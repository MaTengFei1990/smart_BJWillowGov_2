package com.hollysmart.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.hollysmart.bjwillowgov.R;
import com.hollysmart.bjwillowgov.XieYiActivity;
import com.hollysmart.utils.Mlog;


public class ScreenViewDialog extends Dialog implements View.OnClickListener {

    private Context mContext;

    public ScreenViewDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.mContext = context;
    }

    private TextView web_message;
    private TextView tv_ok;
    private TextView tv_back;

    private String message;
    private OnClickListener onClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View mView = LayoutInflater.from(mContext).inflate(R.layout.dialog_screen, null);
        setContentView(mView);

        message = "<p style=\"text-indent:2em;\"><span><span style=\"font-size:14px;line-height:2;\">" +
                "欢迎您使用杨柳飞絮防治，为了加强对您个人信息的保护，依据最新的监管要求，\n" +
                "我们更新了隐私政策，以向您说明我们在收集和使用您的相关个人信息时的处理规则。 \n" +
                "1.为给您提供基本服务,我们可能会申请手机存储权限、摄像头权限、麦克风权限;\n" +
                "2.为了基于您所在的位置向您推荐内容,我们可能会申请您的位置权限;\n" +
                "3.为了信息推送和账号安全,我们会申请系统设备权限收集设备信息、日志信息;\n" +
                "4.为了帮助您发现更多好友,我们可能会申请通讯录权限;\n" +
                "您可以阅读完整版</span><span style=\"color:#337FE5;\"><a href=\"ncsti.fuwu\" target=\"_blank\"><span style=\"font-size:14px;line-height:2;\">" +
                "“服务协议”</span></a></span><span style=\"font-size:14px;line-height:2;\">及</span><span style=\"color:#337FE5;\"><a href=\"ncsti.yinsi\" target=\"_blank\">" +
                "<span style=\"font-size:14px;line-height:2;\"> “隐私政策”</span></a></span>" +
                "<span style=\"font-size:14px;\"><span style=\"font-size:14px;line-height:2;\"></span>" +
                "<span></span><span style=\"font-size:14px;line-height:2;\"></span></span></span></p>";

        web_message = mView.findViewById(R.id.web_message);
        tv_ok = mView.findViewById(R.id.tv_ok);
        tv_ok.setOnClickListener(this);
        tv_back = mView.findViewById(R.id.tv_back);
        tv_back.setOnClickListener(this);

        web_message.setText(getClickableHtml(message));
        //这一句很重要，否则ClickableSpan内的onClick方法将无法触发！！
        web_message.setMovementMethod(LinkMovementMethod.getInstance());

    }


    /**
     * 格式化超链接文本内容并设置点击处理
     */
    private CharSequence getClickableHtml(String html) {
        Spanned spannedHtml = Html.fromHtml(html);
        SpannableStringBuilder clickableHtmlBuilder = new SpannableStringBuilder(spannedHtml);
        URLSpan[] urls = clickableHtmlBuilder.getSpans(0, spannedHtml.length(), URLSpan.class);
        for (final URLSpan span : urls) {
            setLinkClickable(clickableHtmlBuilder, span);
        }
        return clickableHtmlBuilder;
    }

    /**
     * 设置点击超链接对应的处理内容
     */
    private void setLinkClickable(final SpannableStringBuilder clickableHtmlBuilder, final URLSpan urlSpan) {
        int start = clickableHtmlBuilder.getSpanStart(urlSpan);
        int end = clickableHtmlBuilder.getSpanEnd(urlSpan);
        int flags = clickableHtmlBuilder.getSpanFlags(urlSpan);

        ClickableSpan clickableSpan = new ClickableSpan() {
            public void onClick(View view) {
                String url = urlSpan.getURL();
                Mlog.d("拦截到的 URL = " + url);
                if (url.contains("fuwu")) {
                    Intent intent = new Intent(mContext, XieYiActivity.class);
                    intent.putExtra("id", "2190");
                    intent.putExtra("title", "服务协议");
                    mContext.startActivity(intent);
                } else if (url.contains("yinsi")) {
                    Intent intent = new Intent(mContext, XieYiActivity.class);
                    intent.putExtra("id", "2191");
                    intent.putExtra("title", "隐私政策");
                    mContext.startActivity(intent);
                }
            }
        };
        clickableHtmlBuilder.setSpan(clickableSpan, start, end, flags);
    }

    public void setOnClickOkListener(OnClickListener onClickOkListener) {
        this.onClickListener = onClickOkListener;
    }

    @Override
    public void onClick(View v) {
        if (onClickListener != null) {
            switch (v.getId()) {
                case R.id.tv_ok:
                    onClickListener.OnClickOK(v);
                    break;
                case R.id.tv_back:
                    onClickListener.OnClickBack(v);
                    break;
            }
        }
        cancel();
    }

    public interface OnClickListener {
        void OnClickOK(View view);

        void OnClickBack(View view);
    }

}