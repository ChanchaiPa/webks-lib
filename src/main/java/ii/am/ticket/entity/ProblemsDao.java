package ii.am.ticket.entity;

import java.util.Date;
import java.util.List;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.QueryBuilder.JoinWhereOperation;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

import ii.am.ticket.AppUtils;
import ii.am.ticket.ReminderUtils;

public class ProblemsDao {
	private ConnectionSource connectionSource = null;

	public ProblemsDao(ConnectionSource connectionSource) {
		this.connectionSource = connectionSource;
	}
	
	public int findMaxId() throws Exception {
		Dao<ProblemsEntity, Integer> problemsDao = DaoManager.createDao(connectionSource, ProblemsEntity.class);
		return Math.toIntExact( problemsDao.queryRawValue("select MAX(ticket_id) from problems") );
	}
	
	public int create(ProblemsEntity pbEntity) throws Exception {
		Dao<ProblemsEntity, Integer> problemsDao = DaoManager.createDao(connectionSource, ProblemsEntity.class);
		return problemsDao.create(pbEntity);
	}
	
	public int update(ProblemsEntity pbEntity) throws Exception {
		Dao<ProblemsEntity, Integer> problemsDao = DaoManager.createDao(connectionSource, ProblemsEntity.class);
		return problemsDao.update(pbEntity);
	}	
	
	public ProblemsEntity findById(int ticket_id) throws Exception {
		Dao<ProblemsEntity, Integer> problemsDao = DaoManager.createDao(connectionSource, ProblemsEntity.class);
		return problemsDao.queryForId(ticket_id);
	}
	
	public int changeStatus(int ticket_id, int status_id, int agent_id) throws Exception {
		//String current = AppUtils.toDateFormat(new Date(), "dd-MM-yyyy HH:mm:ss");
		//String cmd = "update problems set problem_status_id=?, modified_date=TO_TIMESTAMP('"+current+"', 'DD-MM-YYYY HH24:MI:SS') where set ticket_id=?";
		Dao<ProblemsEntity, Integer> problemsDao = DaoManager.createDao(connectionSource, ProblemsEntity.class);
		UpdateBuilder<ProblemsEntity, Integer> builder = problemsDao.updateBuilder();
		builder.updateColumnValue("problem_status_id", status_id);
		builder.updateColumnValue("modified_date", new Date());
		if (status_id==2)
			builder.updateColumnValue("close_date", new Date());
		if (agent_id>0)
			builder.updateColumnValue("owner_id", agent_id);		
		builder.where().eq("ticket_id", ticket_id);
		return builder.update();
	}
	
	
	//*********************************
	public int countByOwner(int owner_id) throws Exception {
		Dao<ProblemsEntity, Integer> problemsDao = DaoManager.createDao(connectionSource, ProblemsEntity.class);
		Long count = problemsDao
				.queryBuilder().where().eq("owner_id", owner_id).and().in("problem_status_id", "1,3,4")
				.countOf();
		return count.intValue();
	}	
	
	public List<ProblemsEntity> findByOwner(int pageNo, int pageSize, int owner_id) throws Exception {
		Long LIMIT = Long.valueOf(pageSize);
		Long OFFSET = (pageNo-1) * LIMIT;
		Dao<ProblemsEntity, Integer> problemsDao = DaoManager.createDao(connectionSource, ProblemsEntity.class);
		List<ProblemsEntity> list = problemsDao
				.queryBuilder().where().eq("owner_id", owner_id).and().in("problem_status_id", "1,3,4")
				.queryBuilder().orderBy("ticket_id", true)
				.offset( OFFSET ).limit( LIMIT )
				.query();
		list.forEach(item -> { item.setAgePercent( ReminderUtils.agePercent(item.getOpen_date(), item.getReminder_date())); });
		return list;
	}
	
	
	//*********************************
	public int countByAddUserinProcessing(int add_user_id) throws Exception {
		Dao<ProblemsEntity, Integer> problemsDao = DaoManager.createDao(connectionSource, ProblemsEntity.class);
		Long count = problemsDao
				.queryBuilder().where().eq("add_user_id", add_user_id).and().ne("owner_id", add_user_id).and().in("problem_status_id", "1,3,4")
				.countOf();
		return count.intValue();
	}	
	
	public List<ProblemsEntity> findByAddUserinProcessing(int pageNo, int pageSize, int add_user_id) throws Exception {
		Long LIMIT = Long.valueOf(pageSize);
		Long OFFSET = (pageNo-1) * LIMIT;
		Dao<ProblemsEntity, Integer> problemsDao = DaoManager.createDao(connectionSource, ProblemsEntity.class);
		List<ProblemsEntity> list = problemsDao
				.queryBuilder().where().eq("add_user_id", add_user_id).and().ne("owner_id", add_user_id).and().in("problem_status_id", "1,3,4")
				.queryBuilder().orderBy("ticket_id", true)
				.offset( OFFSET ).limit( LIMIT )
				.query();
		list.forEach(item -> { item.setAgePercent( ReminderUtils.agePercent(item.getOpen_date(), item.getReminder_date())); });
		return list;
	}	
	
	
	//*********************************
	public int countByTracking(Date fr_open_date, Date to_open_date, int ticket_id, int problem_status_id, String customer_id, int system_id, int subsystem_id, int item_id,
			int agent_id, int level_id, int group_id) throws Exception {
		Dao<ProblemsEntity, Integer> problemsDao = DaoManager.createDao(connectionSource, ProblemsEntity.class);
		QueryBuilder<ProblemsEntity, Integer> builder = problemsDao.queryBuilder();
		
		Where<ProblemsEntity, Integer> where = builder.where();
		where.between("open_date", fr_open_date, to_open_date);
		if (ticket_id > 0)
			where.and().eq("ticket_id", ticket_id);
		if (problem_status_id > 0)
			where.and().eq("problem_status_id", problem_status_id);
		if (customer_id.length() > 0)
			where.and().eq("customer_id", customer_id);		
		if (system_id > 0)
			where.and().eq("system_id", system_id);
		if (subsystem_id > 0)
			where.and().eq("subsystem_id", subsystem_id);
		if (item_id > 0)
			where.and().eq("item_id", item_id);	
		
		if (level_id == 1) {
			where.and();
			where.raw("(add_user_id=? or owner_id=?)", new SelectArg(SqlType.INTEGER, agent_id), new SelectArg(SqlType.INTEGER, agent_id));	
		}
		if (level_id == 2) {
			where.and();
			where.raw("(add_user_id=(select agent_id from agents where group_id=?) or owner_id=(select agent_id from agents where group_id=?))", new SelectArg(SqlType.INTEGER, group_id), new SelectArg(SqlType.INTEGER, group_id));							
		}
		
		Long count = builder.countOf();
		return count.intValue();
	}	
	
	public List<ProblemsEntity> findByTracking(Date fr_open_date, Date to_open_date, int ticket_id, int problem_status_id, String customer_id, int system_id, int subsystem_id, int item_id,
			int agent_id, int level_id, int group_id, 
			int pageNo, int pageSize) throws Exception {
		Long LIMIT = Long.valueOf(pageSize);
		Long OFFSET = (pageNo-1) * LIMIT;
		Dao<ProblemsEntity, Integer> problemsDao = DaoManager.createDao(connectionSource, ProblemsEntity.class);
		QueryBuilder<ProblemsEntity, Integer> builder = problemsDao.queryBuilder();
		
		Where<ProblemsEntity, Integer> where = builder.where();
		where.between("open_date", fr_open_date, to_open_date);
		if (ticket_id > 0)
			where.and().eq("ticket_id", ticket_id);
		if (problem_status_id > 0)
			where.and().eq("problem_status_id", problem_status_id);
		if (customer_id.length() > 0)
			where.and().eq("customer_id", customer_id);		
		if (system_id > 0)
			where.and().eq("system_id", system_id);
		if (subsystem_id > 0)
			where.and().eq("subsystem_id", subsystem_id);
		if (item_id > 0)
			where.and().eq("item_id", item_id);	
		
		if (level_id == 1) {
			where.and();
			where.raw("(add_user_id=? or owner_id=?)", new SelectArg(SqlType.INTEGER, agent_id), new SelectArg(SqlType.INTEGER, agent_id));			
		}
		if (level_id == 2) {
			where.and();
			where.raw("(add_user_id=(select agent_id from agents where group_id=?) or owner_id=(select agent_id from agents where group_id=?))", new SelectArg(SqlType.INTEGER, group_id), new SelectArg(SqlType.INTEGER, group_id));				
		}
		
		List<ProblemsEntity> list = builder.orderBy("ticket_id", true).offset( OFFSET ).limit( LIMIT ).query();
		list.forEach(item -> { item.setAgePercent( ReminderUtils.agePercent(item.getOpen_date(), item.getReminder_date())); });
		return list;
	}		
	
}
