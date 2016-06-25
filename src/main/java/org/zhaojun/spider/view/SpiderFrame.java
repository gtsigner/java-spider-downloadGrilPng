package org.zhaojun.spider.view;

import org.zhaojun.spider.controller.SpiderController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by zhaojun on 2016/6/25.
 */
public class SpiderFrame extends JFrame {

    private JButton mBtnStart;
    private JButton mBtnStop;
    private GridLayout gridLayout;
    private SpiderController yySpider;

    public SpiderFrame() throws HeadlessException {

        this.setBounds(300, 300, 500, 300);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        yySpider = new SpiderController();

        this.initComp();

    }


    //初始化组件
    private void initComp() {

        gridLayout = new GridLayout();
        gridLayout.setColumns(2);
        gridLayout.setRows(1);
        this.getContentPane().setLayout(gridLayout);
        this.mBtnStart = new JButton("开始YY");
        this.mBtnStop = new JButton("停止YY");
        this.mBtnStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                yySpider.start();
            }
        });

        this.mBtnStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                yySpider.stop();
            }
        });

        /**
         * 释放资源
         */
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                yySpider.destroy();
            }
        });

        this.add("1", mBtnStart);
        this.add("1", mBtnStop);
    }

    public void go() {

        this.setVisible(true);
    }


}
