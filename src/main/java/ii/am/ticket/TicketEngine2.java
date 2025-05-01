package ii.am.ticket;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

import ii.am.ticket.model.SearchModel;
import ii.am.ticket.model.TransferModel;

public class TicketEngine2 {
	private final String dtFormat= "dd-MM-yyyy HH:mm:ss";
	private String connectionUrl = "";

	public TicketEngine2(String connectionUrl) {
		this.connectionUrl = connectionUrl;
	}
	
	public TicketEngine2(String db_host, String db_user, String db_pass, String db_name) {
		this.connectionUrl = "jdbc:postgresql://"+ db_host +"/"+ db_name +"?user="+ db_user +"&password=" + db_pass+"&ssl=false";
	}

	
	
	public String reminderInfo(int system_id, int subsystem_id, int item_id, int module_id, String openDate) {
		String cmd = "select need_day, need_hr from system_guide where system_id=" + system_id;
		if (subsystem_id > 0)
			cmd = "select need_day, need_hr from subsystem_guide where system_id=" + system_id + " and subsystem_id=" + subsystem_id;
		if (item_id > 0)
			cmd = "select need_day, need_hr from item_guide where system_id=" + system_id + " and subsystem_id=" + subsystem_id + " and item_id=" + item_id;	
		if (module_id > 0)
			cmd = "select need_day, need_hr from module_guide where system_id=" + system_id + " and subsystem_id=" + subsystem_id + " and item_id=" + item_id + " and module_id=" + module_id;	
		
		int need_day=1, need_hr=0;
		Connection conn = null;
		try {
			Class.forName("org.postgresql.Driver");
			conn = java.sql.DriverManager.getConnection( connectionUrl ); 	
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			if (rs.next()) {
				need_day = rs.getInt("need_day");
				need_hr  = rs.getInt("need_hr");
			}
			rs.close();
			stmt.close();
		}
		catch(Exception e) {
			//
		}
		finally {
			try { conn.close(); } catch(Exception e) {}
		}
		
		Date reminderDate = ReminderUtils.getReminderDate(AppUtils.parseDateTime(openDate, dtFormat), need_day, need_hr);
		JSONObject result = new JSONObject();
		result.put("need_day", need_day);
		result.put("need_hr" , need_hr);
		result.put("reminder_date", AppUtils.toDateFormat(reminderDate, dtFormat));
		return result.toString();
	}
	
	
	
	public String getInBox(int logon_agent_id) {
		/*String fieldName = type.equals("inbox") ? "to_user_id" : "transfered_by";
		String cmd = "select p.ticket_id, p.open_date, p.reminder_date, p.need_day, p.need_hr, substring(p.problem_detail,1,50), p.system_id, "
				+ "tr.trans_date, tr.transfered_by, tr.to_user_id, tr.to_group_id "
				+ "from problem_trans tr, problems p where tr.ticket_id=p.ticket_id and tr."+ fieldName +"=0 and tr.transfered_by="+ logon_agent_id +" order by p.ticket_id";*/
		String cmd = "SELECT tr.trans_id, tr.trans_date, tr.transfered_by, tr.to_user_id, tr.to_group_id, a1.login as transfered_by_name, a2.login as to_user_name, g1.name as to_group_name, "
				+ "p.ticket_id, p.open_date, p.reminder_date, p.need_day, p.need_hr, substring(p.problem_detail,1,50) as problem_detail, p.system_id, s1.description as system_id_desc "
				+ "FROM problem_trans tr "
				+ "LEFT JOIN problems p ON tr.ticket_id=p.ticket_id "
				+ "LEFT JOIN agents a1 ON tr.transfered_by=a1.agent_id "
				+ "LEFT JOIN agents a2 ON tr.to_user_id=a2.agent_id "
				+ "LEFT JOIN groups g1 ON tr.to_group_id=g1.group_id "
				+ "LEFT JOIN system_guide s1 ON p.system_id=s1.system_id "
				+ "where tr.to_picked=0 and tr.to_user_id="+ logon_agent_id +" order by tr.trans_id";
		//System.out.println(cmd);
		ArrayList<TransferModel> list = new ArrayList<>();
		Connection conn = null;
		try {
			Class.forName("org.postgresql.Driver");
			conn = java.sql.DriverManager.getConnection( connectionUrl ); 	
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
				TransferModel model = new TransferModel();
				model.setTrans_id( rs.getInt("trans_id") );
				model.setTrans_date( rs.getTimestamp("trans_date") );
				model.setTransfered_by( rs.getInt("transfered_by") );
				model.setTo_user_id( rs.getInt("to_user_id") );
				model.setTo_group_id( rs.getInt("to_group_id") );
				model.setTransfered_by_name( rs.getString("transfered_by_name") );
				model.setTo_user_name( rs.getString("to_user_name") );
				model.setTo_group_name( rs.getString("to_group_name") );
				model.setTicket_id( rs.getInt("ticket_id") );
				model.setOpen_date( rs.getTimestamp("open_date") );
				model.setReminder_date( rs.getTimestamp("reminder_date") );
				model.setNeed_day( rs.getInt("need_day") );
				model.setNeed_hr( rs.getInt("need_hr") );
				model.setProblem_detail( rs.getString("problem_detail") );
				model.setSystem_id( rs.getInt("system_id") );
				model.setSystem_id_desc( rs.getString("system_id_desc") );
				list.add(model);
			}
			rs.close();
			stmt.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			try { conn.close(); } catch(Exception e) {}
		}
		
		list.forEach(item -> { item.setAgePercent( ReminderUtils.agePercent(item.getOpen_date(), item.getReminder_date())); });
		try {
		    ObjectMapper mapper = new ObjectMapper();	
		    mapper.setTimeZone(TimeZone.getDefault());
			return mapper.writeValueAsString(list);	
		}
		catch(Exception e) {
			return e.getMessage();
		}
	}
	
	public String getOutBox(int logon_agent_id, int pageNo, int pageSize, int totalRec) {
		int LIMIT = pageSize;
		int OFFSET = (pageNo-1) * LIMIT;  
		String cmd1 = "select count(*) as i from problem_trans where to_picked=0 and transfered_by=" + logon_agent_id;
		String cmd2 = "SELECT tr.trans_id, tr.trans_date, tr.transfered_by, tr.to_user_id, tr.to_group_id, a1.login as transfered_by_name, a2.login as to_user_name, g1.name as to_group_name, "
				+ "p.ticket_id, p.open_date, p.reminder_date, p.need_day, p.need_hr, substring(p.problem_detail,1,50) as problem_detail, p.system_id, s1.description as system_id_desc "
				+ "FROM problem_trans tr "
				+ "LEFT JOIN problems p ON tr.ticket_id=p.ticket_id "
				+ "LEFT JOIN agents a1 ON tr.transfered_by=a1.agent_id "
				+ "LEFT JOIN agents a2 ON tr.to_user_id=a2.agent_id "
				+ "LEFT JOIN groups g1 ON tr.to_group_id=g1.group_id "
				+ "LEFT JOIN system_guide s1 ON p.system_id=s1.system_id "
				+ "where tr.to_picked=0 and tr.transfered_by="+ logon_agent_id +" order by p.ticket_id, tr.trans_id OFFSET "+OFFSET+" LIMIT " + LIMIT;
		
		ArrayList<TransferModel> list = new ArrayList<>();
		Connection conn = null;
		Statement stmt  = null;
		try {
			Class.forName("org.postgresql.Driver");
			conn = java.sql.DriverManager.getConnection( connectionUrl ); 	
			stmt = conn.createStatement();
			if (totalRec == 0) {
				ResultSet rs1 = stmt.executeQuery(cmd1);
				if (rs1.next())
					totalRec = rs1.getInt("i");
				rs1.close();
			}
				
			ResultSet rs2 = stmt.executeQuery(cmd2);
			while (rs2.next()) {
				TransferModel model = new TransferModel();
				model.setTrans_id( rs2.getInt("trans_id") );
				model.setTrans_date( rs2.getTimestamp("trans_date") );
				model.setTransfered_by( rs2.getInt("transfered_by") );
				model.setTo_user_id( rs2.getInt("to_user_id") );
				model.setTo_group_id( rs2.getInt("to_group_id") );
				model.setTransfered_by_name( rs2.getString("transfered_by_name") );
				model.setTo_user_name( rs2.getString("to_user_name") );
				model.setTo_group_name( rs2.getString("to_group_name") );
				model.setTicket_id( rs2.getInt("ticket_id") );
				model.setOpen_date( rs2.getTimestamp("open_date") ); //Calendar.getInstance(java.util.TimeZone.getTimeZone("ICT"))
				model.setReminder_date( rs2.getTimestamp("reminder_date") );
				model.setNeed_day( rs2.getInt("need_day") );
				model.setNeed_hr( rs2.getInt("need_hr") );
				model.setProblem_detail( rs2.getString("problem_detail") );
				model.setSystem_id( rs2.getInt("system_id") );
				model.setSystem_id_desc( rs2.getString("system_id_desc") );
				list.add(model);				
			}
			rs2.close();
		}
		catch(Exception e) {
			//
		}
		finally {
			try { stmt.close(); } catch(Exception e) {}
			try { conn.close(); } catch(Exception e) {}
		}
		
		list.forEach(item -> { item.setAgePercent( ReminderUtils.agePercent(item.getOpen_date(), item.getReminder_date())); });
		SearchModel<TransferModel> result = new SearchModel<>();
		result.setTotalRec(totalRec);
		result.setList(list);
		try {
		    ObjectMapper mapper = new ObjectMapper();	
		    mapper.setTimeZone(TimeZone.getDefault());
			return mapper.writeValueAsString(result);	
		}
		catch(Exception e) {
			return e.getMessage();
		}
	}	
	
	
	
	public String ssimGenTree() {
		JSONArray currList = new JSONArray();
		Connection conn = null;
		try {
			Class.forName("org.postgresql.Driver");
			conn = java.sql.DriverManager.getConnection( connectionUrl ); 	
			Statement stmt = conn.createStatement();
			
			String cmd1 = "select system_id as key, description as title, null as parent, "
					+ "CONCAT(need_day, '-', need_hr, '-', is_active) as moreinfo from system_guide order by system_id";
			ResultSet rs1 = stmt.executeQuery(cmd1);
			while (rs1.next()) {				
				setNode(currList, rs1.getString("key"), rs1.getString("title"), rs1.getString("parent"), rs1.getString("moreinfo"));
			}   
			rs1.close();
			
			String cmd2 = "select CONCAT(system_id, '-', subsystem_id) as key, description as title, system_id as parent, "
					+ "CONCAT(need_day, '-', need_hr, '-', is_active) as moreinfo from subsystem_guide order by system_id, subsystem_id";
			ResultSet rs2 = stmt.executeQuery(cmd2);
			while (rs2.next()) {
				setNode(currList, rs2.getString("key"), rs2.getString("title"), rs2.getString("parent"), rs2.getString("moreinfo"));
			}   
			rs2.close();
			
			String cmd3 = "select CONCAT(system_id, '-', subsystem_id, '-', item_id) as key, description as title, CONCAT(system_id, '-', subsystem_id) as parent, "
					+ "CONCAT(need_day, '-', need_hr, '-', is_active) as moreinfo from item_guide order by system_id, subsystem_id, item_id";
			ResultSet rs3 = stmt.executeQuery(cmd3);
			while (rs3.next()) {
				setNode(currList, rs3.getString("key"), rs3.getString("title"), rs3.getString("parent"), rs3.getString("moreinfo"));
			}   
			rs3.close();		
			
			String cmd4 = "select CONCAT(system_id, '-', subsystem_id, '-', item_id, '-', module_id) as key, description as title, CONCAT(system_id, '-', subsystem_id, '-', item_id) as parent, "
					+ "CONCAT(need_day, '-', need_hr, '-', is_active) as moreinfo from module_guide order by system_id, subsystem_id, item_id, module_id";
			ResultSet rs4 = stmt.executeQuery(cmd4);
			while (rs4.next()) {
				setNode(currList, rs4.getString("key"), rs4.getString("title"), rs4.getString("parent"), rs4.getString("moreinfo"));
			}   
			rs4.close();			
			stmt.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			try { conn.close(); } catch(Exception e) {}
		}
		return currList.toString();
	}
	
	private void setNode(JSONArray currList, String _key, String _title, String _parent, String _moreinfo) {
		if (_parent == null) {
			JSONObject currNode = new JSONObject();
			currNode.put("key", _key);
			currNode.put("title", _title);	
			currNode.put("moreinfo", _moreinfo);
			currList.put(currNode);				
		}
		else {
			for (Object _node: currList) {
				JSONObject currNode = (JSONObject)_node;
				if (_parent.equals( currNode.getString("key") )) {
					JSONObject childNode = new JSONObject();
					childNode.put("key", _key);
					childNode.put("title", _title);	
					childNode.put("moreinfo", _moreinfo);
					if(!currNode.has("children")) 
						currNode.put("children", new JSONArray());
					currNode.getJSONArray("children").put(childNode);	
					//System.out.println(_key + " - " + _title);
					break;
				}
				else {
					if (currNode.has("children")) 
						setNode(currNode.getJSONArray("children"), _key, _title, _parent, _moreinfo);
				}				
			}
		}
	}
	
}
