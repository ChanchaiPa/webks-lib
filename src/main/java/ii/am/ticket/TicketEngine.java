package ii.am.ticket;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

import ii.am.ticket.entity.ProblemTransDao;
import ii.am.ticket.entity.ProblemTransEntity;
import ii.am.ticket.entity.ProblemsDao;
import ii.am.ticket.entity.ProblemsEntity;
import ii.am.ticket.model.BaseModel;
import ii.am.ticket.model.SearchModel;


public class TicketEngine {
	private final String dtFormat= "dd-MM-yyyy HH:mm:ss";
	private String connectionUrl = "";
	private ConnectionSource connectionSource = null;

	public TicketEngine(String db_host, String db_user, String db_pass, String db_name) throws Exception {
		connectionUrl = "jdbc:postgresql://"+ db_host +"/"+ db_name +"?user="+ db_user +"&password=" + db_pass+"&ssl=false";	//jdbc:postgresql://192.168.187.134:5432/postgres?user=postgres&password=postgres_pass
		connectionSource = new JdbcConnectionSource( connectionUrl );
	}
	
	
	public String getTicket(int ticketId, int logon_agent_id) {  // logon_agent_id Optional
		ProblemsDao problemsDao = new ProblemsDao(connectionSource);
		ProblemTransDao probTransDao = new ProblemTransDao(connectionSource);
		try {
			ProblemsEntity entity = problemsDao.findById( ticketId );
			if (entity.getProblem_status_id()==4 && logon_agent_id>0) 
				entity.setCountInbox( Math.toIntExact(probTransDao.countInbox_TicketAgent(ticketId, logon_agent_id)) );
			
		    ObjectMapper mapper = new ObjectMapper();	
		    mapper.setTimeZone(TimeZone.getDefault());
			return mapper.writeValueAsString(entity);
		}
		catch(Exception e) {
			return e.getMessage();
		}
	}
	
	public String newTicket(String def_call_code, int def_severity_level, int def_priority_level, int def_system_code, int agent_id, int group_id) {
		Date currDate = new Date();
		TicketEngine2 engine2 = new TicketEngine2(connectionUrl);
		JSONObject jsonReminder = new JSONObject( engine2.reminderInfo(def_system_code, 0, 0, 0, AppUtils.toDateFormat(currDate, dtFormat)) );
		
		ProblemsDao problemsDao = new ProblemsDao(connectionSource);
		int maxId = 0;
		try { maxId = problemsDao.findMaxId()+1; }
		catch(Exception e) { System.out.println(e.getMessage()); }
		
		ProblemsEntity entity = new ProblemsEntity();
		entity.setTicket_id( maxId );
		entity.setOpen_date( currDate );
		entity.setAdd_user_id( agent_id );
		entity.setOwner_id( agent_id );
		entity.setGroup_id( group_id );
		entity.setCustomer_id( null );
		entity.setCustomer_name( null );
		entity.setCaller_name( null );
		entity.setCaller_phoneno( null );
		entity.setSystem_id( def_system_code );
		entity.setSubsystem_id( null );
		entity.setItem_id( null );
		entity.setModule_id( null );
		entity.setCall_code( def_call_code );
		entity.setSeverity_level( def_severity_level );
		entity.setPriority_level( def_priority_level );
		entity.setReminder_date( AppUtils.parseDateTime(jsonReminder.getString("reminder_date"), dtFormat) );
		entity.setNeed_day( jsonReminder.getInt("need_day") );
		entity.setNeed_hr( jsonReminder.getInt("need_hr") );
		entity.setProblem_detail( null );
		entity.setResolved_detail( null );
		entity.setModified_date( currDate ); 
		entity.setClose_date( null );
		entity.setProblem_status_id( 1 );
		
		try {
			problemsDao.create(entity);
		    ObjectMapper mapper = new ObjectMapper();	
		    mapper.setTimeZone(TimeZone.getDefault());
			return mapper.writeValueAsString(entity);
		}
		catch(Exception e) {
			return e.getMessage();
		}
	}
	
	public String updateTicket(String strTicket) {
		Date currDate = new Date();
		ProblemsDao problemsDao = new ProblemsDao(connectionSource);
		try {
			JSONObject result = new JSONObject(strTicket);
			int ticket_id = result.getInt("ticket_id");
			int need_day = result.getInt("need_day");
			int need_hr  = result.getInt("need_hr");		
			String close_date = AppUtils.jsonGetString(result, "close_date");
			int problem_status_id = result.getInt("problem_status_id");
			
			ProblemsEntity entity = problemsDao.findById( ticket_id );
			entity.setCustomer_id( AppUtils.jsonGetString(result, "customer_id") );
			entity.setCustomer_name( AppUtils.jsonGetString(result, "customer_name") );
			entity.setCaller_name( AppUtils.jsonGetString(result, "caller_name") );
			entity.setCaller_phoneno( AppUtils.jsonGetString(result, "caller_phoneno") );
			entity.setSystem_id( result.getInt("system_id") );
			entity.setSubsystem_id( AppUtils.jsonGetInt(result, "subsystem_id") );
			entity.setItem_id( AppUtils.jsonGetInt(result, "item_id") );
			entity.setModule_id( AppUtils.jsonGetInt(result, "module_id") );
			entity.setCall_code(  result.getString("call_code")  );
			entity.setSeverity_level( result.getInt("severity_level") );
			entity.setPriority_level( result.getInt("priority_level") );
			entity.setReminder_date( AppUtils.parseDateTime(result.getString("reminder_date"), dtFormat) );
			entity.setNeed_day( need_day );
			entity.setNeed_hr( need_hr );
			entity.setProblem_detail( AppUtils.jsonGetString(result, "problem_detail") );
			entity.setResolved_detail( AppUtils.jsonGetString(result, "resolved_detail") );
			entity.setModified_date( currDate );
			if (problem_status_id==2 && (close_date==null || close_date.equals("")))
				entity.setClose_date( currDate );
			if (problem_status_id==1)
				problem_status_id= 3;
			entity.setProblem_status_id( problem_status_id );
			
			problemsDao.update(entity);
		    ObjectMapper mapper = new ObjectMapper();	
		    mapper.setTimeZone(TimeZone.getDefault());
			return mapper.writeValueAsString(entity);
		}
		catch(Exception e) {
			return e.getMessage();
		}
	}
	
	
	
	public String transferTicket(int logon_agent_id, String strJson) {
		Integer ticket_id = 0;
		String type = "";
		ArrayList<BaseModel> selectedList = new ArrayList<>();
		Connection conn = null;
		try {
			JSONObject result = new JSONObject(strJson);
			ticket_id = result.getInt("ticket_id");
			type = result.getString("type");
			JSONArray list = result.getJSONArray("list");
			if (type.equals("Group")) {
				String tmp = "";
				for (Object groupid: list)
					tmp += (String)groupid + ",";
				String cmd = "select agent_id, group_id from agents where group_id in ("+ tmp.subSequence(0, tmp.length()-1) +")";
				Class.forName("org.postgresql.Driver");
				conn = java.sql.DriverManager.getConnection( connectionUrl ); 	
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(cmd);
				while (rs.next()) {
					int agent_id = rs.getInt("agent_id");
					int group_id = rs.getInt("group_id");
					if (agent_id!= logon_agent_id)
						selectedList.add(new BaseModel(Integer.toString(agent_id), Integer.toString(group_id), null));
				}
				rs.close();
			}
			else { //Agent
				for (Object item: list)
					selectedList.add(new BaseModel(item+"", null, null));
			}	
			
		}
		catch(Exception e) { e.printStackTrace();
			return e.getMessage();
		}	
		finally {
			try { conn.close(); } catch(Exception e) {}
		}
		
		Date currDate = new Date();
		ProblemsDao problemsDao = new ProblemsDao(connectionSource);
		ProblemTransDao problemTransDao = new ProblemTransDao(connectionSource);
		int count = 0;
		try {
			for (BaseModel item: selectedList) {
				ProblemTransEntity entity = new ProblemTransEntity();
				entity.setTicket_id(ticket_id);	
				
				entity.setTrans_date(currDate);
				entity.setTo_user_id( Integer.parseInt(item.getCode()) );
				entity.setTo_group_id( item.getName()==null ? null : Integer.parseInt(item.getName()) );
				entity.setTransfered_by(logon_agent_id);
				entity.setTrans_type(type.equals("Group") ? 1 : 2);
				problemTransDao.create(entity);
				count++;
			}
			problemsDao.changeStatus(ticket_id, 4, -1); //Transfer
		}
		catch(Exception e) {
			return e.getMessage();
		}
		return Integer.toString(count);
	}
	
	public String takeOwnerTicket(int ticket_id, int take_agent_id) {
		ProblemsDao problemsDao = new ProblemsDao(connectionSource);
		ProblemTransDao problemTransDao = new ProblemTransDao(connectionSource);
		try {
			problemsDao.changeStatus(ticket_id, 3, take_agent_id);//Pending
			String result = problemTransDao.takeOwner(ticket_id, take_agent_id);
			return result; //'Success' or 'Currenty Take Owner by=x'
		}
		catch(Exception e) {
			return e.getMessage();
		}
	}
	
	public String voidTicket(int ticket_id) {
		ProblemsDao problemsDao = new ProblemsDao(connectionSource);
		try {
			problemsDao.changeStatus(ticket_id, 6, 0);//Void
			return "Success";
		}
		catch(Exception e) {
			return e.getMessage();
		}
	}
	
	public String closeTicket(int ticket_id) {
		ProblemsDao problemsDao = new ProblemsDao(connectionSource);
		try {
			problemsDao.changeStatus(ticket_id, 2, 0);//Close
			return "Success";
		}
		catch(Exception e) {
			return e.getMessage();
		}
	}	
	
	
	
	public String ticketOnhand(int pageNo, int pageSize, int totalRec, int agent_id) {
		ProblemsDao problemsDao = new ProblemsDao(connectionSource);
		try {
			if (totalRec == 0)
				totalRec = problemsDao.countByOwner(agent_id);
			List<ProblemsEntity> list = problemsDao.findByOwner(pageNo, pageSize, agent_id);
			
			SearchModel<ProblemsEntity> search = new SearchModel<ProblemsEntity>();
			search.setTotalRec(totalRec);
			search.setList(list);
			
		    ObjectMapper mapper = new ObjectMapper();	
		    mapper.setTimeZone(TimeZone.getDefault());
			return mapper.writeValueAsString(search);
		}
		catch(Exception e) {
			return e.getMessage();
		}
	}
	
	public String ticketInProcessing(int pageNo, int pageSize, int totalRec, int agent_id) {
		ProblemsDao problemsDao = new ProblemsDao(connectionSource);
		try {
			if (totalRec == 0)
				totalRec = problemsDao.countByAddUserinProcessing(agent_id);
			List<ProblemsEntity> list = problemsDao.findByAddUserinProcessing(pageNo, pageSize, agent_id);
			
			SearchModel<ProblemsEntity> search = new SearchModel<ProblemsEntity>();
			search.setTotalRec(totalRec);
			search.setList(list);
			
		    ObjectMapper mapper = new ObjectMapper();	
		    mapper.setTimeZone(TimeZone.getDefault());
			return mapper.writeValueAsString(search);
		}
		catch(Exception e) {
			return e.getMessage();
		}
	}	
	
	
	public String ticketTracking(String strJson, int agent_id, int level_id, int group_id ) {		
		ProblemsDao problemsDao = new ProblemsDao(connectionSource);
		try {
			JSONObject json = new JSONObject(strJson);
		    int pageNo   = json.getInt("pageNo"); 
		    int pageSize = json.getInt("pageSize");
			int totalRec = json.getInt("totalRec");
			
		    Date fr_open_date = AppUtils.isoDateTime(json.getString("fr_open_date")); 
			Date to_open_date = AppUtils.isoDateTime(json.getString("to_open_date"));
		    int ticket_id = AppUtils.toInt(json.getString("ticket_id"), 0);
		    int problem_status_id= json.getInt("problem_status_id");
		    String customer_id= json.getString("customer_id");
		    int system_id = json.getInt("system_id");
		    int subsystem_id  = json.getInt("subsystem_id");
		    int item_id  = json.getInt("item_id");
			
			if (totalRec == 0)
				totalRec = problemsDao.countByTracking(fr_open_date, to_open_date, ticket_id, problem_status_id, customer_id, system_id, subsystem_id, item_id,
							agent_id, level_id, group_id);
			List<ProblemsEntity> list = problemsDao.findByTracking(fr_open_date, to_open_date, ticket_id, problem_status_id, customer_id, system_id, subsystem_id, item_id,
					agent_id, level_id, group_id, 
					pageNo, pageSize);
			
			SearchModel<ProblemsEntity> search = new SearchModel<ProblemsEntity>();
			search.setTotalRec(totalRec);
			search.setList(list);
			
		    ObjectMapper mapper = new ObjectMapper();	
		    mapper.setTimeZone(TimeZone.getDefault());
			return mapper.writeValueAsString(search);
		}
		catch(Exception e) {
			return e.getMessage();
		}	
	}
	
	
	public String closeTicket(int ticket_id, int take_agent_id) {
		ProblemsDao problemsDao = new ProblemsDao(connectionSource);
		ProblemTransDao problemTransDao = new ProblemTransDao(connectionSource);
		try {
			problemsDao.changeStatus(ticket_id, 3, take_agent_id);//Pending
			String result = problemTransDao.takeOwner(ticket_id, take_agent_id);
			return result; //'Success' or 'Currenty Take Owner by=x'
		}
		catch(Exception e) {
			return e.getMessage();
		}
	}
	
}	




