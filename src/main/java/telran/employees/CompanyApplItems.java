package telran.employees;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import telran.view.InputOutput;
import telran.view.Item;
import telran.view.Menu;

import static telran.employees.CompanyConfigProperties.*;

public class CompanyApplItems {
	static Company company;
	static HashSet<String> departments;
	static String[] emplTypes;
	static Map<String, Function<Employee, Employee>> emplTypesMap;

	public static List<Item> getCompanyItems(Company company, HashSet<String> departments, 
			String[] emplTypes, Map<String, Function<Employee, Employee>> emplTypesMap) {
		CompanyApplItems.company = company;
		CompanyApplItems.departments = departments;
		CompanyApplItems.emplTypes = emplTypes;
		CompanyApplItems.emplTypesMap = emplTypesMap;
		Item[] items = { 
				Item.of("add employee", CompanyApplItems::addEmployee),
				Item.of("add employee (with Map)", CompanyApplItems::addEmployee),
				Item.of("display employee data", CompanyApplItems::getEmployee),
				Item.of("remove employee", CompanyApplItems::removeEmployee),
				Item.of("display department budget", CompanyApplItems::getDepartmentBudget),
				Item.of("display departments", CompanyApplItems::getDepartments),
				Item.of("display managers with most factor", CompanyApplItems::getManagersWithMostFactor), };
		return new ArrayList<>(List.of(items));

	}

	static void addEmployee(InputOutput io) {
		displaySubmenu(io, "Add new employee", emplTypes, emplType -> {
	        try {
	        	Method method = CompanyApplItems.class.getDeclaredMethod("get" + emplType, Employee.class, InputOutput.class);
	        	Employee empl = readEmployee(io);	
	        	Employee result = (Employee) method.invoke(null, empl, io);
				addEmployeeToCompany(io, result);
			} catch (Exception e) {
				io.writeLine("This type of Employee doesn't exist");
			}	        
		}, true, false);
	}

    static void addEmployeeWithMap(InputOutput io) {
        displaySubmenu(io, "Add new employee", emplTypesMap.keySet().toArray(String[]::new), emplType -> {
            Employee empl = readEmployee(io);
            Employee result = emplTypesMap.get(emplType).apply(empl);
            addEmployeeToCompany(io, result);
        }, true, false);
    }

	private static void addEmployeeToCompany(InputOutput io, Employee result) {
		try {
			company.addEmployee(result);
			io.writeLine("Employee has been added");
		} catch (Exception e) {
			io.writeLine(e.getMessage());
		}
	}

	protected static Employee getSalesPerson(Employee empl, InputOutput io) {
		WageEmployee wageEmployee = (WageEmployee) getWageEmployee(empl, io);
		float percents = io.readNumberRange("Enter percents", "Wrong percents value", MIN_PERCENT, MAX_PERCENT)
				.floatValue();
		long sales = io.readNumberRange("Enter sales", "Wrong sales value", MIN_SALES, MAX_SALES).longValue();
		return new SalesPerson(empl.getId(), empl.getBasicSalary(), empl.getDepartment(), wageEmployee.getHours(),
				wageEmployee.getWage(), percents, sales);
	}

	protected static Employee getManager(Employee empl, InputOutput io) {
		float factor = io.readNumberRange("Enter factor", "Wrong factor value", MIN_FACTOR, MAX_FACTOR).floatValue();
		return new Manager(empl.getId(), empl.getBasicSalary(), empl.getDepartment(), factor);
	}

	protected static Employee getWageEmployee(Employee empl, InputOutput io) {
		int hours = io.readNumberRange("Enter working hours", "Wrong hours value", MIN_HOURS, MAX_HOURS).intValue();
		int wage = io.readNumberRange("Enter hour wage", "Wrong wage value", MIN_WAGE, MAX_WAGE).intValue();
		return new WageEmployee(empl.getId(), empl.getBasicSalary(), empl.getDepartment(), hours, wage);
	}

	private static Employee readEmployee(InputOutput io) {
		long id = readEmployeeId(io);
		int basicSalary = io
				.readNumberRange("Enter basic salary", "Wrong basic salary", MIN_BASIC_SALARY, MAX_BASIC_SALARY)
				.intValue();
		String department = readDepartmentSubMenu(io);
		return new Employee(id, basicSalary, department);
	}

	private static String readDepartmentSubMenu(InputOutput io) {
		final String[] departmentHolder = new String[1];
	    displaySubmenu(io, "Departments", departments.toArray(String[]::new), department -> {
	        departmentHolder[0] = department;
	    }, true, false);
		return departmentHolder[0];
	}

	private static long readEmployeeId(InputOutput io) {
		return Long.parseLong(io.readStringPredicate("Enter id value", 
				"Wrong id value or employee exists already", 
				id -> {
					long parsedId = Long.parseLong(id);
					return parsedId >= MIN_ID && parsedId <= MAX_ID && company.getEmployee(parsedId) == null;
				}));
	}

	static void getEmployee(InputOutput io) {
		long id = readEmployeeId(io);
		Employee empl = company.getEmployee(id);
		String line = empl == null ? "no employee with the entered ID" : empl.getJSON();
		io.writeLine(line);
	}

	static void removeEmployee(InputOutput io) {
		long id = readEmployeeId(io);
		Employee empl = company.removeEmployee(id);
		io.writeLine(empl);
		io.writeLine("has been removed from the company\n");
	}


	static void getDepartmentBudget(InputOutput io) {
		displaySubmenu(io, "Departments", departments.toArray(String[]::new), department -> {
			printBudget(io, department);
		}, false, true);
	}

	private static void printBudget(InputOutput io, String department) {
		int budget = company.getDepartmentBudget(department);
		String line = budget == 0 ? "no employees woring in entered department"
				: "Budget of enetered department is " + budget;
		io.writeLine(line);
	}

	private static <T> void displaySubmenu(InputOutput io, String menuTitle, T[] items, 
			Consumer<T> itemAction, boolean isExit, boolean isExitToMenu) {
		List<Item> menuItems = Arrays.stream(items)
				.map(item -> Item.of(item.toString(), action -> itemAction.accept(item), isExit))
				.collect(Collectors.toList());
		if (isExitToMenu) {
			menuItems.add(Item.of("Exit to main menu", action -> {
			}, true));
		}
		Menu menu = new Menu(menuTitle, menuItems.toArray(Item[]::new));
		menu.perform(io);
	}

	static void getDepartments(InputOutput io) {
		String[] departments = company.getDepartments();
		String line = departments.length == 0 ? "no employees" : String.join("\n", departments);
		io.writeLine(line);
	}

	static void getManagersWithMostFactor(InputOutput io) {
		Manager[] managers = company.getManagersWithMostFactor();
		String line = managers.length == 0 ? "no managers"
				: Arrays.stream(managers).map(Employee::getJSON).collect(Collectors.joining("\n"));
		io.writeLine(line);
	}	
}
