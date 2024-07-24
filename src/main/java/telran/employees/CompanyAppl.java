package telran.employees;

import telran.io.Persistable;
import telran.view.*;

import java.util.*;
import java.util.function.Function;
public class CompanyAppl {

	private static final String FILE_NAME = "employeesTest.data";

	public static void main(String[] args) {
		Company company = new CompanyMapsImpl();
		try {
			((Persistable)company).restore(FILE_NAME);
		} catch (Exception e) {
			
		}
		
        Map<String, Function<Employee, Employee>> emplTypes = new HashMap<>();
        emplTypes.put("WageEmployee", e -> CompanyApplItems.getWageEmployee(e, new SystemInputOutput()));
        emplTypes.put("Manager", e -> CompanyApplItems.getManager(e, new SystemInputOutput()));
        emplTypes.put("SalesPerson", e -> CompanyApplItems.getSalesPerson(e, new SystemInputOutput()));
		
        List<Item> companyItems = CompanyApplItems.getCompanyItems(company,
                new HashSet<>(List.of("Audit", "Development", "QA")),
                new String[]{"WageEmployee", "Manager", "SalesPerson", "Revisor"}, emplTypes);
		companyItems.add(Item.of("Exit & save",
				io -> ((Persistable)company).save(FILE_NAME), true));
		companyItems.add(Item.ofExit());
		Menu menu = new Menu("Company CLI Application",
				companyItems.toArray(Item[]::new));
		menu.perform(new SystemInputOutput());

	}

}
