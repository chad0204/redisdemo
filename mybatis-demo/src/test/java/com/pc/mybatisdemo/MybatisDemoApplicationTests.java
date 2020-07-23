package com.pc.mybatisdemo;

import com.pc.mybatisdemo.mapper.Service;
import com.pc.mybatisdemo.mapper.UserMapper;
import com.pc.mybatisdemo.model.UserEntity;
import com.pc.mybatisdemo.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.sql.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MybatisDemoApplicationTests {

	@Autowired
	private UserService userService;



	@Test
	public void getUser() {

		for(int i=0;i<10;i++) {
			UserEntity user = userService.getUser(1L,null);
			System.out.println(user.getUserName());
		}


	}


	@Test
	public void testJDBC() throws ClassNotFoundException, SQLException {

		Class.forName("com.mysql.cj.jdbc.Driver");
		System.out.println("数据库驱动加载成功");


		Connection connection =  DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=UTC","root","root");
		System.out.println("数据库连接成功");

		Statement stmt  =  connection.createStatement();

		ResultSet resultSet = stmt.executeQuery("select * from t_user");

		while (resultSet.next()) {
			System.out.println(resultSet.getString("user_name"));
		}


		connection.close();
		stmt.close();


	}





}
