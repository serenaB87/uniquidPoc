package com.uniquid.register.impl.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.register.circle.CircleChannel;
import com.uniquid.register.circle.CircleRegister;
import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.guest.GuestChannel;
import com.uniquid.register.guest.GuestsRegister;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;
import com.uniquid.register.user.UserChannel;
import com.uniquid.register.user.UserRegister;

/**
 * Data Access Object concrete class implementation of {@code ProviderRegister} and {@code UserRegister} that uses
 * SQLite as data store.
 */
public class SQLiteRegister implements ProviderRegister, UserRegister, CircleRegister, GuestsRegister {
	
	public static final String CREATE_PROVIDER_TABLE = "create table provider_channel (provider_address text not null, user_address text not null, bitmask text not null, revoke_address text not null, revoke_tx_id text not null, creation_time integer not null, primary key (provider_address, user_address));";

	public static final String CREATE_USER_TABLE = "create table user_channel (provider_name text not null, provider_address text not null, user_address text not null, bitmask text not null, revoke_address text not null, revoke_tx_id text not null, primary key (provider_name, provider_address, user_address));";

	public static final String CREATE_CIRCLES_TABLE = "create table circles (id INTEGER PRIMARY KEY AUTOINCREMENT, name text not null);";
	
	public static final String CREATE_GUESTS_TABLE = "create table guests (id INTEGER PRIMARY KEY AUTOINCREMENT, code text, name text, isActive integer DEFAULT 0, idCircle INTEGER NOT NULL, FOREIGN KEY(idCircle) REFERENCES circles(id));";
	
	private static final String PROVIDER_CHANNEL_BY_USER = "select provider_address, user_address, bitmask, revoke_address, revoke_tx_id, creation_time from provider_channel where user_address = ?";
	
	private static final String PROVIDER_CHANNEL_BY_REVOKE_ADDRESS = "select provider_address, user_address, bitmask, revoke_address, revoke_tx_id, creation_time from provider_channel where revoke_address = ?";
	
	private static final String PROVIDER_CHANNEL_BY_REVOKE_TXID = "select provider_address, user_address, bitmask, revoke_address, revoke_tx_id, creation_time from provider_channel where revoke_tx_id = ?";
	
	private static final String PROVIDER_INSERT = "insert into provider_channel (provider_address, user_address, bitmask, revoke_address, revoke_tx_id, creation_time) values (?, ?, ?, ?, ?, ?);";
	
	private static final String PROVIDER_DELETE = "delete from provider_channel where provider_address = ? and user_address = ?;";
	
	private static final String PROVIDER_ALL_CHANNEL = "select provider_address, user_address, bitmask, revoke_address, revoke_tx_id, creation_time from provider_channel order by creation_time desc;";
	
	
	private static final String USER_ALL_CHANNEL = "select provider_name, provider_address, user_address, bitmask, revoke_address, revoke_tx_id from user_channel";
	
	private static final String USER_CHANNEL_BY_NAME = "select provider_name, provider_address, user_address, bitmask, revoke_address, revoke_tx_id from user_channel where provider_name = ?;";

	private static final String USER_CHANNEL_BY_ADDRESS = "select provider_name, provider_address, user_address, bitmask, revoke_address, revoke_tx_id from user_channel where provider_address = ?;";

	private static final String USER_CHANNEL_BY_REVOKE_TXID = "select provider_name, provider_address, user_address, bitmask, revoke_address, revoke_tx_id from user_channel where revoke_tx_id = ?;";

	private static final String USER_CHANNEL_BY_REVOKE_ADDRESS = "select provider_name, provider_address, user_address, bitmask, revoke_address, revoke_tx_id from user_channel where revoke_address = ?;";

	private static final String INSERT_USER_CHANNEL = "insert into user_channel (provider_name, provider_address, user_address, bitmask, revoke_address, revoke_tx_id) values (?, ?, ?, ?, ?, ?);";

	private static final String USER_CHANNEL_DELETE = "delete from user_channel where provider_name = ? and provider_address = ? and user_address = ?";
	
	private static final String CIRCLE_DELETE = "delete from circles where name = ?";
	
	private static final String INSERT_CIRCLE = "insert into circles (name) values (?)";
	
	private static final String CIRCLE_BY_MASTER_ID = "select id, name from circles where name = ?;"; 
	
	private static final String ACTIVE_GUEST_BY_CIRCLES_ID = "select id, idCircle, name, code, isActive from guests where isActive = 0 and idCircle in (select id from circles where name = ?);";
	
	private static final String INSERT_GUEST = "insert into guests (code, name, idCircle) values (?, ?, (select id from circles where name = ?));";

	private static final Logger LOGGER = LoggerFactory.getLogger(SQLiteRegister.class.getName());

	protected BasicDataSource dataSource;
	
	/**
	 * Creates an instance from the connection
	 * @param dataSource the connection to use
	 */
	SQLiteRegister(BasicDataSource dataSource) {
		
		Validate.notNull(dataSource);
		
		this.dataSource = dataSource;
		
	}
	
	/**
	 * Helper method
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	private ProviderChannel providerChannelFromResultSet(ResultSet rs) throws SQLException {
		
		ProviderChannel providerChannel = new ProviderChannel();

		providerChannel.setProviderAddress(rs.getString("provider_address"));
		providerChannel.setUserAddress(rs.getString("user_address"));
		providerChannel.setBitmask(rs.getString("bitmask"));
		providerChannel.setRevokeAddress(rs.getString("revoke_address"));
		providerChannel.setRevokeTxId(rs.getString("revoke_tx_id"));
		providerChannel.setCreationTime(rs.getLong("creation_time"));

		return providerChannel;
		
	}
	
	/*
	 * Helper method
	 */
	private ResultSetHandler<ProviderChannel> createProviderResultSetHandler() {
		
		return new ResultSetHandler<ProviderChannel>() {
			
			@Override
			public ProviderChannel handle(ResultSet rs) throws SQLException {
				
				if (rs.next()) {

					return providerChannelFromResultSet(rs);
					
				}
				
				return null;
			}
		};
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ProviderChannel> getAllChannels() throws RegisterException {
		
		ResultSetHandler<List<ProviderChannel>> handler = new ResultSetHandler<List<ProviderChannel>>() {
			
			@Override
			public List<ProviderChannel> handle(ResultSet rs) throws SQLException {
				List<ProviderChannel> providerChannels = new ArrayList<ProviderChannel>();
				
				while (rs.next()) {

					ProviderChannel providerChannel = providerChannelFromResultSet(rs);

					providerChannels.add(providerChannel);

				}
				
				return providerChannels;
			}
		};
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
		
			return run.query(PROVIDER_ALL_CHANNEL, handler);
		
		} catch (SQLException ex) {
			
			throw new RegisterException("Exception while getAllChannels()", ex);
			
		}
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ProviderChannel getChannelByUserAddress(String address) throws RegisterException {
		
		if (!StringUtils.isNotBlank(address)) {
			
			throw new RegisterException("address is not valid");
		
		}
		
		ResultSetHandler<ProviderChannel> handler = createProviderResultSetHandler();
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			
			return run.query(PROVIDER_CHANNEL_BY_USER, handler, address);
		
		} catch (SQLException ex) {

			throw new RegisterException("Exception while getChannelByUserAddress()", ex);
			
		}

	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ProviderChannel getChannelByRevokeAddress(String revokeAddress) throws RegisterException {
		
		if (!StringUtils.isNotBlank(revokeAddress)) {
			
			throw new RegisterException("revokeAddress is not valid");
		
		}
		
		ResultSetHandler<ProviderChannel> handler = createProviderResultSetHandler();
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			
			return run.query(PROVIDER_CHANNEL_BY_REVOKE_ADDRESS, handler, revokeAddress);
		
		} catch (SQLException ex) {

			throw new RegisterException("Exception while getChannelByRevokeAddress()", ex);
			
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public ProviderChannel getChannelByRevokeTxId(String revokeTxId) throws RegisterException {
		
		if (!StringUtils.isNotBlank(revokeTxId)) {
			
			throw new RegisterException("revokeTxId is not valid");
		
		}
		
		ResultSetHandler<ProviderChannel> handler = createProviderResultSetHandler();
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			
			return run.query(PROVIDER_CHANNEL_BY_REVOKE_TXID, handler, revokeTxId);
		
		} catch (SQLException ex) {

			throw new RegisterException("Exception while getChannelByRevokeTxId()", ex);
			
		}
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void insertChannel(ProviderChannel providerChannel) throws RegisterException {
		
		if (providerChannel == null) throw new RegisterException("providerChannel is null!");
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			
			run.update(PROVIDER_INSERT, providerChannel.getProviderAddress(),
					providerChannel.getUserAddress(), providerChannel.getBitmask(), providerChannel.getRevokeAddress(),
					providerChannel.getRevokeTxId(), providerChannel.getCreationTime());
		
		} catch (SQLException ex) {

			throw new RegisterException("Exception while insertChannel()", ex);
			
		}
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteChannel(ProviderChannel providerChannel) throws RegisterException {
		
		if (providerChannel == null) throw new RegisterException("providerChannel is null!");
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			
			run.update(PROVIDER_DELETE, providerChannel.getProviderAddress(),
					providerChannel.getUserAddress());
		
		} catch (SQLException ex) {

			throw new RegisterException("Exception while deleteChannel()", ex);
			
		}
		
	}
	
	/**
	 * Helper method
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	private UserChannel userChannelFromResultSet(ResultSet rs) throws SQLException {
		
		UserChannel userChannel = new UserChannel();

		userChannel.setProviderName(rs.getString("provider_name"));
		userChannel.setProviderAddress(rs.getString("provider_address"));
		userChannel.setUserAddress(rs.getString("user_address"));
		userChannel.setBitmask(rs.getString("bitmask"));
		userChannel.setRevokeAddress(rs.getString("revoke_address"));
		userChannel.setRevokeTxId(rs.getString("revoke_tx_id"));

		return userChannel;
		
	}
	
	/**
	 * Helper method
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	private CircleChannel circleChannelFromResultSet(ResultSet rs) throws SQLException {
		
		CircleChannel circleChannel = new CircleChannel();

		circleChannel.setId(rs.getInt("id"));
		circleChannel.setName(rs.getString("name"));
		
		return circleChannel;
		
	}
	
	
	/**
	 * Helper method
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	private GuestChannel guestChannelFromResultSet(ResultSet rs) throws SQLException {
		
		GuestChannel guestChannel = new GuestChannel();

		guestChannel.setId(rs.getInt("id"));
		guestChannel.setIdCircle(rs.getInt("idCircle"));
		guestChannel.setName(rs.getString("name"));
		guestChannel.setCode(rs.getString("code"));
		Integer isActiveInt = rs.getInt("isActive");
		if(isActiveInt==0){
			guestChannel.setIsActive(true);
		}else{
			guestChannel.setIsActive(false);
		}
		
		return guestChannel;
		
	}
	
	
	/*
	 * Helper method
	 */
	private ResultSetHandler<UserChannel> createUserResultSetHandler() {
		
		return new ResultSetHandler<UserChannel>() {
			
			@Override
			public UserChannel handle(ResultSet rs) throws SQLException {
				
				if (rs.next()) {

					return userChannelFromResultSet(rs);
					
				}
				
				return null;
			}
		};
	}

	/*
	 * Helper method
	 */
	private ResultSetHandler<CircleChannel> createCircleResultSetHandler() {
		
		return new ResultSetHandler<CircleChannel>() {
			
			@Override
			public CircleChannel handle(ResultSet rs) throws SQLException {
				
				if (rs.next()) {

					return circleChannelFromResultSet(rs);
					
				}
				
				return null;
			}
		};
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<UserChannel> getAllUserChannels() throws RegisterException {
		
		ResultSetHandler<List<UserChannel>> handler = new ResultSetHandler<List<UserChannel>>() {
			
			List<UserChannel> userChannels = new ArrayList<UserChannel>();
			
			@Override
			public List<UserChannel> handle(ResultSet rs) throws SQLException {
				
				while (rs.next()) {

					UserChannel userChannel = userChannelFromResultSet(rs);

					userChannels.add(userChannel);

				}
				
				return userChannels;
				
			}
		};
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			
			return run.query(USER_ALL_CHANNEL, handler);
		
		} catch (SQLException ex) {

			throw new RegisterException("Exception while getAllUserChannels()", ex);
			
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserChannel getChannelByName(String name) throws RegisterException {
		
		if (!StringUtils.isNotBlank(name)) {
			
			throw new RegisterException("name is not valid");
		
		}
		
		ResultSetHandler<UserChannel> handler = createUserResultSetHandler();
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			
			return run.query(USER_CHANNEL_BY_NAME, handler, name);
		
		} catch (SQLException ex) {

			throw new RegisterException("Exception while getChannelByName()", ex);
			
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserChannel getChannelByProviderAddress(String name) throws RegisterException {
		
		if (!StringUtils.isNotBlank(name)) {
			
			throw new RegisterException("name is not valid");
		
		}
		
		ResultSetHandler<UserChannel> handler = createUserResultSetHandler();
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			
			return run.query(USER_CHANNEL_BY_ADDRESS, handler, name);
		
		} catch (SQLException ex) {

			throw new RegisterException("Exception while getChannelByProviderAddress()", ex);
			
		}
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void insertChannel(UserChannel userChannel) throws RegisterException {
		
		if (userChannel == null) throw new RegisterException("userchannel is null!");
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			
			run.update(INSERT_USER_CHANNEL, userChannel.getProviderName(), userChannel.getProviderAddress(),
					userChannel.getUserAddress(), userChannel.getBitmask(), userChannel.getRevokeAddress(),
					userChannel.getRevokeTxId() );
		
		} catch (SQLException ex) {

			throw new RegisterException("Exception while insertChannel()", ex);
			
		}
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void deleteChannel(UserChannel userChannel) throws RegisterException {
		
		if (userChannel == null) throw new RegisterException("userchannel is null!");
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			
			run.update(USER_CHANNEL_DELETE, userChannel.getProviderName(),
					userChannel.getProviderAddress(), userChannel.getUserAddress() );
		
		} catch (SQLException ex) {

			throw new RegisterException("Exception while deleteChannel()", ex);
			
		}
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserChannel getUserChannelByRevokeTxId(String revokeTxId) throws RegisterException {
		
		if (!StringUtils.isNotBlank(revokeTxId)) {
			
			throw new RegisterException("revokeTxId is not valid");
		
		}
		
		ResultSetHandler<UserChannel> handler = createUserResultSetHandler();
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			
			return run.query(USER_CHANNEL_BY_REVOKE_TXID, handler, revokeTxId);
		
		} catch (SQLException ex) {

			throw new RegisterException("Exception while getUserChannelByRevokeTxId()", ex);
			
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UserChannel getUserChannelByRevokeAddress(String revokeAddress) throws RegisterException {
		
		if (!StringUtils.isNotBlank(revokeAddress)) {
			
			throw new RegisterException("revokeAddress is not valid");
		
		}
		
		ResultSetHandler<UserChannel> handler = createUserResultSetHandler();
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			
			return run.query(USER_CHANNEL_BY_REVOKE_ADDRESS, handler, revokeAddress);
		
		} catch (SQLException ex) {

			throw new RegisterException("Exception while getUserChannelByRevokeAddress()", ex);
			
		}

	}
	
	/**
	 * Returns QueryRunner to use for interact with DB
	 */
	protected TransactionAwareQueryRunner getQueryRunner() {
		
		return new TransactionAwareQueryRunner(dataSource);
		
	}

	@Override
	public void insertCircle(String masterId) throws RegisterException {
		if (!StringUtils.isNotBlank(masterId)) {
			
			throw new RegisterException("masterId is not valid");
		
		}
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			String name = masterId + "_circle";
			run.update(INSERT_CIRCLE, name);
		
		} catch (SQLException ex) {

			throw new RegisterException("Exception while insertChannel()", ex);
			
		}
	}

	@Override
	public void deleteCircle(CircleChannel circle) throws RegisterException {
		
		if (circle == null) throw new RegisterException("CircleChannel is null!");
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			
			run.update(CIRCLE_DELETE, circle.getName() );
		
		} catch (SQLException ex) {

			throw new RegisterException("Exception while deleteChannel()", ex);
			
		}
	}

	@Override
	public CircleChannel getCircle(String masterId) throws RegisterException {
		if (!StringUtils.isNotBlank(masterId)) {
			
			throw new RegisterException("masterId is not valid");
		
		}
		
		ResultSetHandler<CircleChannel> handler = new ResultSetHandler<CircleChannel>() {
			
			@Override
			public CircleChannel handle(ResultSet rs) throws SQLException {
				CircleChannel circleChannel = new CircleChannel();
				
				while (rs.next()) {
					circleChannel = circleChannelFromResultSet(rs);
				}
				
				return circleChannel;
				
			}
		};
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			String name = masterId + "_circle";
			return run.query(CIRCLE_BY_MASTER_ID, handler, name);
		
		} catch (SQLException ex) {

			throw new RegisterException("Exception while getCircle()", ex);
			
		}
		
	}
	

	@Override
	public Boolean checkExists(String masterId) throws RegisterException {
		if (!StringUtils.isNotBlank(masterId)) {
			
			throw new RegisterException("masterId is not valid");
		
		}
		Boolean result = false;
		
		ResultSetHandler<List<CircleChannel>> handler = new ResultSetHandler<List<CircleChannel>>() {
			
			List<CircleChannel> circleChannels = new ArrayList<CircleChannel>();
			
			@Override
			public List<CircleChannel> handle(ResultSet rs) throws SQLException {
				
				while (rs.next()) {

					CircleChannel circleChannel = circleChannelFromResultSet(rs);

					circleChannels.add(circleChannel);

				}
				return circleChannels;
				
			}
		};
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			String name = masterId + "_circle";
			List<CircleChannel> circles = run.query(CIRCLE_BY_MASTER_ID, handler, name);
			if(!circles.isEmpty()){
				result = true;
			}
			return result;
		} catch (SQLException ex) {

			throw new RegisterException("Exception while c()", ex);
			
		}
		
	}


	@Override
	public void insertGuest(GuestChannel guest, String masterId) throws RegisterException {
		if (guest == null) throw new RegisterException("guestChannel is null!");
		if (!StringUtils.isNotBlank(masterId)) {
			
			throw new RegisterException("masterId is not valid");
		
		}
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			run.update(INSERT_GUEST, guest.getName(), guest.getCode(), guest.getIdCircle());
		
		} catch (SQLException ex) {

			throw new RegisterException("Exception while insertGuestChannel()", ex);
			
		}
	}

	@Override
	public List<GuestChannel> getGuestsByCircleName(String masterId) throws RegisterException {
		if (!StringUtils.isNotBlank(masterId)) {
			
			throw new RegisterException("masterId is not valid");
		
		}
		
		ResultSetHandler<List<GuestChannel>> handler = new ResultSetHandler<List<GuestChannel>>() {
			
			List<GuestChannel> guestChannels = new ArrayList<GuestChannel>();
			
			@Override
			public List<GuestChannel> handle(ResultSet rs) throws SQLException {
				
				while (rs.next()) {

					GuestChannel guestChannel = guestChannelFromResultSet(rs);

					guestChannels.add(guestChannel);

				}
				
				return guestChannels;
				
			}
		};
		
		TransactionAwareQueryRunner run = getQueryRunner();
		
		try {
			String name = masterId + "_circle";
			return run.query(ACTIVE_GUEST_BY_CIRCLES_ID, handler, name);
		
		} catch (SQLException ex) {

			throw new RegisterException("Exception while getGuestsByCircle()", ex);
			
		}
	};

}
