package com.bps.service.core;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class SessionFactoryManager {

	private SessionFactoryManager() {
	}
	private static SessionFactory sessionFactory = null;
	public static SessionFactory getSessionFactory() {
		if (sessionFactory == null) {
			System.out.println("Loading Session factory for first time.");
			final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
					.configure().build();
			try {
				sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
			}
			catch (Exception e) {
				e.printStackTrace();
				StandardServiceRegistryBuilder.destroy(registry);
			}
		}
		return sessionFactory;
	}
}
