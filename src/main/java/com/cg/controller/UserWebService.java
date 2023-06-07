package com.cg.controller;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.cg.exception.EmployeeNotFoundException;
import com.cg.model.Employee;
import com.cg.response.ResponseInfo;
import com.cg.utility.GlobalResources;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;



@RestController
public class UserWebService {
	@Autowired
	RestTemplate restTemplate;

	// URL of the Microsorvice which we invoke
	String endPoint="http://localhost:8081/api/employees";

	//create logger variable which present in Globally resources package
	private Logger logger = GlobalResources.getLogger(UserWebService.class);
	
	@RequestMapping("/welcome")
	public String firstPage() {
		return "Wlcome to Home Page";
	}

	/*
	 * for Adding or Create Employee
	 */
	@RequestMapping(value = "/employee", method = RequestMethod.POST)
	ResponseEntity<ResponseInfo> addEmployee(@RequestBody Employee employee){

		//  Logger Implemention
		String methodName="addEmployee()";
		logger.info(methodName + "Called");

		return restTemplate.postForEntity(endPoint, employee, ResponseInfo.class);
	}

	/*
	 * for Updating the Employee
	 */
	@RequestMapping(value = "/employee/{id}", method = RequestMethod.PUT)
	String updateEmployee(@RequestBody Employee employee) throws EmployeeNotFoundException {

		//  Logger Implemention
		String methodName="updateEmployee()";
		logger.info(methodName + "Called");

		restTemplate.put(endPoint, employee, ResponseInfo.class);
		return "Updated successfully";
	}

	/*
	 * for Deleting the Employee by Id
	 */
	@RequestMapping(value = "employee/{id}", method = RequestMethod.DELETE)
	public String deleteEmployee(@PathVariable("id") int id) {

		//  Logger Implemention
		String methodName="deleteEmployee()";
		logger.info(methodName + "Called");

		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		HttpEntity<Employee> entity = new HttpEntity<Employee>(headers);

		return restTemplate.exchange(
				"http://localhost:8081/api/employees/"+id, HttpMethod.DELETE, entity, String.class).getBody();
	}
	
	/*
	 * for getting all the employee
	 */
	@RequestMapping(value = "employee", method = RequestMethod.GET)
	@CircuitBreaker(name = "Getting_Employee", fallbackMethod = "employeeGettingFallback")
	List<Employee> getAllEmployee(){

		//  Logger Implemention
		String methodName="getAllEmployee()";
		logger.info(methodName + "Called");

		List<Employee> list=Arrays.asList(restTemplate.getForObject(endPoint, Employee[].class));
		return list;
	}
	
	//creating fallback method for circuit breaker
	public List<Employee> employeeGettingFallback(Exception e){
		logger.info("fall back method is executed because service is down: ", e.getMessage());
		List<Employee> employee = (List<Employee>) new Employee(1, "Dumy", "Dummy@gamil.com");
		return  employee;
		
	}

}
