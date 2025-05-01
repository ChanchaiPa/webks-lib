package ii.am.ticket;

import java.util.Calendar;
import java.util.Properties;
import java.util.TimeZone;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import ii.am.ticket.entity.ProblemsDao;


public class Main {

	public static void main(String[] args) {
		Properties properties = new Properties();
        try {	properties.load(ClassLoader.getSystemResourceAsStream("project.properties"));  } 
        catch (Exception e) {}	
		System.out.println();
		System.out.println("===================================================");
		System.out.println("APPNAME " + properties.getProperty("appname")); 
		System.out.println("VERSION " + properties.getProperty("version"));        
		System.out.println("");
		
		String postgres_host = "192.168.145.128:5432";
		String postgres_password = "postgres_pass";
		//String postgres_host = "perniciously-one-crab.data-1.use1.tembo.io:5432";
		//String postgres_password = "qGyTcLCBt1YJH8zF";
		
		String temp = "";
		try {
			TicketEngine ticketEngine = new TicketEngine(postgres_host, "postgres", postgres_password, "postgres");
			//temp = ticketEngine.reminderInfo(1, 0, 0, 0, "17-01-2025 16:24:34");
			//temp = ticketEngine.newTicket("1", 1, 1, 1, 1, 1);
			//temp = ticketEngine.ticketInProcessing(1, 5, 0, 4);
			//temp = ticketEngine.getTicket( 1, 5 );
			
			///String input = "{\"ticket_id\":3,\"open_date\":\"01-04-2025 11:04:05\",\"add_user_id\":4,\"owner_id\":4,\"group_id\":1,\"customer_id\":\"4105185\",\"customer_name\":\"ชาญชัย\",\"caller_name\":null,\"caller_phoneno\":\"0969612863\",\"system_id\":1,\"subsystem_id\":\"12\",\"item_id\":0,\"module_id\":0,\"call_code\":\"4\",\"severity_level\":1,\"priority_level\":1,\"reminder_date\":\"04-04-2025 16:00:05\",\"need_day\":3,\"need_hr\":4,\"problem_detail\":\"01-04-2025: สอบถามเรื่องการขาย Product xxxx\",\"resolved_detail\":null,\"modified_date\":\"01-04-2025 11:04:05\",\"close_date\":null,\"problem_status_id\":1}";
			//temp = ticketEngine.updateTicket(input);
			//temp = ticketEngine.transferTicket(1, "{\"ticket_id\": 1, \"type\": \"Agent\", \"list\": [1]}");
			//temp = ticketEngine.takeOwnerTicket(1, 99);
			//temp = ticketEngine.ticketOnhand(1, 5, 0, 4);
			
			String input = "{\"pageNo\":1,\"pageSize\":10,\"totalRec\":0,\"fr_open_date\":\"2025-03-27T08:08:38.093Z\",\"to_open_date\":\"2025-04-27T08:08:38.093Z\",\"ticket_id\":\"\",\"problem_status_id\":0,\"customer_id\":\"\",\"system_id\":0,\"subsystem_id\":0,\"item_id\":0}";
			temp = ticketEngine.ticketTracking(input, 1, 3, 1);
			
			System.out.println(temp);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		/*try {
			//TicketEngine ticketEngine = new TicketEngine(postgres_host, "postgres", postgres_password", "postgres");
			//temp = ticketEngine.ssimGenTree();
			//System.out.println(temp);
			
			String connectionUrl = "jdbc:postgresql://"+ postgres_host +"/postgres?user=postgres&password=" + postgres_password+"&ssl=false";	//jdbc:postgresql://192.168.187.134:5432/postgres?user=postgres&password=postgres_pass
			ConnectionSource connectionSource = new JdbcConnectionSource( connectionUrl );
			ProblemsDao problemsDao = new ProblemsDao(connectionSource);	
			problemsDao.changeStatus(1, 3);
		}
		catch(Exception e) {
			e.printStackTrace();
		}*/
		
		try {
			//TicketEngine2 ticketEngine = new TicketEngine2(postgres_host, "postgres", postgres_password, "postgres");
			//temp = ticketEngine.getOutBox(4, 1, 15, 0); // ticketEngine.getInBox(5);
			//System.out.println(temp);
				
		      /*Calendar cal = Calendar.getInstance();
		      String name = cal.getTimeZone().getDisplayName();
		      System.out.println("Default Time Zone: " + name );
		      
		      TimeZone tz = TimeZone.getTimeZone("GMT");
		      cal.setTimeZone(tz);
		      System.out.println("Current Time Zone: " + cal.getTimeZone().getDisplayName());*/
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

}
