package com.geekbrains.work12;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;


import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class MainApp {

    private static final int ITERATIONS = 20_0;
    private static final int ROWS = 10;
    private static final int THREADS = 3;


    public static void main(String[] args) throws IOException {
        SessionFactory factory = new Configuration()
                .configure("hibernate.cfg.xml")
                .buildSessionFactory();


        prepareData(factory);


        ExecutorService executorService = Executors.newFixedThreadPool(THREADS);
        for (int j = 0; j < THREADS; j++) {
            executorService.execute(() -> {
                for (int i = 0; i < ITERATIONS; i++) {
                    try (Session session = factory.getCurrentSession()) {
                        long rowId = (long) (Math.random() * ROWS) + 1;
                        session.beginTransaction();


                        Item item = (Item) session.createQuery(
                                String.format("FROM Item WHERE id = %d", rowId))
                                .setLockMode(LockModeType.PESSIMISTIC_READ)
                                .getSingleResult();
                        item.setVal(item.getVal() + 1);
                        Thread.sleep(5);


                        session.getTransaction().commit();
                    } catch (HibernateException | NoResultException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(180_000, TimeUnit.MILLISECONDS);
            int checkSum = 0;
            for (int i = 1; i <= ROWS; i++) {
                try (Session session = factory.getCurrentSession()) {
                    session.beginTransaction();


                    Item item = (Item) session.createQuery(
                            String.format("FROM Item WHERE id = %d", i))
                            .setLockMode(LockModeType.PESSIMISTIC_READ)
                            .getSingleResult();
                    checkSum += item.getVal();


                    session.getTransaction().commit();
                } catch (HibernateException | NoResultException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Check sum is: " + checkSum);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private static void prepareData(SessionFactory factory) throws IOException {
        try (Session session = factory.getCurrentSession()) {
            String sql = Files
                    .lines(Paths.get("hw_12.sql"))
                    .collect(Collectors.joining(" "));
            session.beginTransaction();
            session.createNativeQuery(sql).executeUpdate();
            session.getTransaction().commit();
        }
    }
}
