package org.zhaojun.spider.view;

import javax.swing.*;
import java.awt.*;

/**
 * Created by zhaojun on 2016/6/25.
 */
public class SpiderFrame extends JFrame {

    private JButton mBtnStart;
    private JButton mBtnStop;
    private GridLayout gridLayout;

    public SpiderFrame() throws HeadlessException {

        this.setBounds(300, 300, 500, 300);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

        this.add("1", mBtnStart);
        this.add("1", mBtnStop);
    }

    public void go() {

        this.setVisible(true);
    }
}
