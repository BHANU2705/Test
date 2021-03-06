package com.bps.controller;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.bps.persistence.tables.Role;
import com.bps.persistence.tables.User;
import com.bps.persistence.tables.UserRole;
import com.bps.persistence.tables.UserRoleEnum;
import com.bps.service.core.ProcessContextPool;
import com.bps.service.core.UserManager;
import com.bps.service.core.email.EmailManager;
import com.bps.service.exceptions.BaseException;
import com.bps.util.CommonConstants;
import com.bps.util.CommonUtility;

@WebServlet(urlPatterns = CommonConstants.URL_HOME_CONTROLLER, name = CommonConstants.HOME)
public class HomeController extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public HomeController() {
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		CommonUtility.logout(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = request.getParameter(CommonConstants.ACTION);
		if (action != null) {
			if (action.equalsIgnoreCase(CommonConstants.LOGIN_ACTION_SIGNIN)) {
				String email = request.getParameter(CommonConstants.EMAIL);
				String password = request.getParameter(CommonConstants.PASSWORD);
				User user = new User(email, password);
				UserManager validator = new UserManager();
				User dbUser = null;
				try {
					dbUser = (User) validator.validate(user);
					if (dbUser != null) {
						ProcessContextPool.get().setUser(dbUser);
						RequestDispatcher dispatcher = request
								.getRequestDispatcher(CommonConstants.URL_MAIN_CONTROLLER);
						request.setAttribute(CommonConstants.EMAIL, email);
						request.setAttribute(CommonConstants.NAME, dbUser.getName());
						HttpSession session = request.getSession();
						session.setAttribute(CommonConstants.EMAIL, email);
						session.setAttribute(CommonConstants.NAME, dbUser.getName());
						dispatcher.forward(request, response);
					} else { // User validation failed
						request.setAttribute(CommonConstants.IS_LOGIN_FAILED, true);
						RequestDispatcher dispatcher = request
								.getRequestDispatcher("/");
						dispatcher.forward(request, response);
					}
				} catch (BaseException e) {
					e.printStackTrace();
				}
			} else if (action.equalsIgnoreCase(CommonConstants.LOGIN_ACTION_SIGNUP)) {
				String email = request.getParameter(CommonConstants.EMAIL);
				String password = request.getParameter(CommonConstants.PASSWORD);
				String name = request.getParameter(CommonConstants.NAME);
				User user = new User(email, password);
				user.setName(name);
				UserManager userManager = new UserManager();
				Role role = new Role();
				role.setId(UserRoleEnum.Admin.toString());
				role.setName(UserRoleEnum.Admin.toString());
				
				Set<UserRole> userRoles = new HashSet<>();
				UserRole userRole = new UserRole();
				userRole.setRole(role);
				userRole.setUser(user);
				
				userRoles.add(userRole);
				user.setUserRoles(userRoles);

				try {
					userManager.createUser(user, null);
					EmailManager emailManager = new EmailManager();
					emailManager.sendSignUpEmail(email);
					ProcessContextPool.get().setUser(user);
					HttpSession session = request.getSession(false);
					session.setAttribute(CommonConstants.EMAIL, user.getEmail());
					session.setAttribute(CommonConstants.NAME, user.getName());
					MainController mainController = new MainController();
					mainController.doPost(request, response);
				} catch (BaseException e) {
					request.setAttribute(CommonConstants.IS_SIGNUP_FAILED, true);
					RequestDispatcher dispatcher = request
							.getRequestDispatcher("/");
					dispatcher.forward(request, response);
				}
			} else {
				// TODO: handle error case
			}
		}
	}
}
