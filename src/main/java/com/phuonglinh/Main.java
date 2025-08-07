package com.phuonglinh;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        SessionFactory sessionFactory = (SessionFactory) context.getBean("sessionFactory");
        try (Session session = sessionFactory.openSession()) {
            if (session != null) {
                System.out.println("Kết nối Hibernate thành công!");
            } else {
                System.out.println("Không thể mở session.");
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi kết nối Hibernate:");
            e.printStackTrace();
        }
        ((ClassPathXmlApplicationContext) context).close();
    }
}
