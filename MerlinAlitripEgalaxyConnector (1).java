package org.mule.modules.merlinalitripegalaxy;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Logger;
import org.mule.api.MuleMessage;
import org.mule.api.annotations.Config;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Processor;
import org.mule.modules.alitrip.AlitripServices;
import org.mule.modules.alitrip.models.TriggerAlitripModels;
import org.mule.modules.egalaxy.EgalaxyServices;
import org.mule.modules.egalaxy.models.QueryOrderResponseModel;
import org.mule.modules.merlinalitripegalaxy.config.ConnectorConfig;
import org.mule.modules.merlinalitripegalaxy.database.Database;
import org.mule.modules.merlinalitripegalaxy.manualconsume.PostmanService;
import org.mule.modules.merlinalitripegalaxy.utils.CSVUtils;

@Connector(name = "merlin-alitrip-egalaxy", friendlyName = "Merlin Alitrip Egalaxy")
public class MerlinAlitripEgalaxyConnector {

	@Config
	ConnectorConfig config;

	// Logger
	final static Logger logger = Logger.getLogger(MerlinAlitripEgalaxyConnector.class);

	private EgalaxyServices egalaxyServices;
	private AlitripServices alitripServices;
	private TriggerAlitripModels triggerAlitripModels;
	private Database database;

	private FileChannel csvWriter;

	@Processor(name = "manualConsume", friendlyName = "Manual Consume")
	public String manualConsume(MuleMessage message) {
		InitiatingServices();
		PostmanService postmanService = new PostmanService();
		try {
			postmanService.getPostmanMessage(message.getPayload().toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// operatorConsume with outerId from database
		database.connect();
		List<String> outerIds = new ArrayList<String>();
		outerIds = postmanService.getListOuterId();

		boolean resultConsume = false;
		// ExecutorService executor = Executors.newFixedThreadPool(5);
		for (int i = 0; i < outerIds.size(); i++) {

			resultConsume = operaterConsume(outerIds.get(i), "", "", "", csvWriter);
			if (resultConsume) {
				database.changeStatusOuterId(outerIds.get(i));
			}

		}

		database.disconnect();

		CSVUtils.writeCsv(csvWriter);
		CSVUtils.endCsv(csvWriter);
		logger.info("manualConsume - END");

		return "";
	}

	@Processor(name = "consume", friendlyName = "consume")
	public void consume() {
		InitiatingServices();

		// operatorConsume with outerId from database
		database.connect();
		List<String> outerIds = new ArrayList<String>();
		outerIds = database.getOuterIds();
		boolean resultConsume = false;

		for (String outerId : outerIds) {
			resultConsume = operaterConsume(outerId, "", "", "", csvWriter);
			if (resultConsume) {
				database.changeStatusOuterId(outerId);
			}
		}

		database.disconnect();

		CSVUtils.writeCsv(csvWriter);
		CSVUtils.endCsv(csvWriter);
		logger.info("consume - END");

		//

		try {
			Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
			while (e.hasMoreElements()) {
				NetworkInterface n = (NetworkInterface) e.nextElement();
				Enumeration<InetAddress> ee = n.getInetAddresses();
				while (ee.hasMoreElements()) {
					InetAddress i = (InetAddress) ee.nextElement();
					if (!i.isLoopbackAddress() && !i.equals(InetAddress.getLocalHost().getHostAddress())) {
						logger.error("HOST IP VALID" + i + ": " + i.getHostAddress());
					} else
						logger.error("HOST IP INVALID" + i + ": " + i.getHostAddress());
				}
			}

		} catch (Exception e) {
			CSVUtils.writeCsv(csvWriter, triggerAlitripModels.getOuterId(), "mule", "alitrip", "send",
					"heartBeatCheck issued", e.getMessage());
			CSVUtils.sendError(triggerAlitripModels.getOuterId(), triggerAlitripModels.getItemId(),
					triggerAlitripModels.getOuterIdSKU(), triggerAlitripModels.getAmount(),
					triggerAlitripModels.getMobile(), e.getMessage(), config.getDestinationEmailAddress(),
					config.getSourceEmailAddress(), config.getUserName(), config.getEmailPassord(),
					config.getMailSmtpHost(), config.getMailSmtpPort());
		}
	}

	/**
	 * connect processor
	 */
	@Processor(name = "connect", friendlyName = "connect")
	public String connect(MuleMessage message) {

		logger.info("connect - START");
		boolean result = false;
		boolean triggerTrue = false;
		InitiatingServices();

		try {
			triggerTrue = triggerAlitripModels.triggerPayloadToModel(message.getPayloadAsString(),
					config.getDestinationEmailAddress(), config.getSourceEmailAddress(), config.getUserName(),
					config.getEmailPassord(), config.getMailSmtpHost(), config.getMailSmtpPort(), csvWriter);
		} catch (Exception ex) {
			logger.info("connect - message.getPayloadAsString ERROR");
		}

		String method = "";
		boolean isVerifySign = false;
		try {
			isVerifySign = triggerAlitripModels.isVerifySign(message.getPayloadAsString(),
					config.getDestinationEmailAddress(), config.getSourceEmailAddress(), config.getUserName(),
					config.getEmailPassord(), config.getMailSmtpHost(), config.getMailSmtpPort(), csvWriter);
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}

		if (isVerifySign) {
			method = triggerAlitripModels.getMethod();
			logger.info(triggerAlitripModels.getOuterId() + "- isVerifySign - Correct");
		} else {
			logger.info(triggerAlitripModels.getOuterId() + "- isVerifySign - NOT Correct");
		}

		String body = "";
		switch (method) {
		case "send":
			logger.info(triggerAlitripModels.getOuterId() + " Case send, method: " + triggerAlitripModels.getMethod());
			if (triggerTrue) {
				logger.info(triggerAlitripModels.getOuterId() + "The trigger is valid with full feld for method=send!");
				try {
					CSVUtils.writeCsv(csvWriter, triggerAlitripModels.getOuterId(), "alitrip", "mule", "send",
							"new ticket trigger", message.getPayloadAsString());
				} catch (Exception e) {
					logger.info("connect - message.getPayloadAsString ERROR");
				}

				result = operaterSend(triggerAlitripModels.getOuterId(), triggerAlitripModels.getItemId(),
						triggerAlitripModels.getTimestamp(), triggerAlitripModels.getAmount(),
						triggerAlitripModels.getToken(), triggerAlitripModels.getOuterIdSKU(),
						config.getEgalaxydetailType(), triggerAlitripModels.getValidStart(),
						triggerAlitripModels.getValidEnd(), triggerAlitripModels.getMobile(), csvWriter);
				if (result) {
					message.setOutboundProperty("http.status", 200);
					body = "{\"code\":\"isv.success-all\", \"msg\":\"success\"}";
				} else {
					CSVUtils.writeCsv(csvWriter, triggerAlitripModels.getOuterId(), "mule", "alitrip", "send",
							"new ticket issued", "HTTP 500 - Internal Server Error");
					CSVUtils.sendError(triggerAlitripModels.getOuterId(), triggerAlitripModels.getItemId(),
							triggerAlitripModels.getOuterIdSKU(), triggerAlitripModels.getAmount(),
							triggerAlitripModels.getMobile(), "HTTP 500 - Internal Server Error",
							config.getDestinationEmailAddress(), config.getSourceEmailAddress(), config.getUserName(),
							config.getEmailPassord(), config.getMailSmtpHost(), config.getMailSmtpPort());
					message.setOutboundProperty("http.status", 500);
				}
				CSVUtils.writeCsv(csvWriter, triggerAlitripModels.getOuterId(), "mule", "alitrip", "response",
						"new ticket trigger", body);

			}
			break;

		case "resend":
			logger.info(
					triggerAlitripModels.getOuterId() + " Case resend, method: " + triggerAlitripModels.getMethod());
			try {
				CSVUtils.writeCsv(csvWriter, triggerAlitripModels.getOuterId(), "alitrip", "mule", "send",
						"ticket resend trigger", message.getPayloadAsString());
			} catch (Exception e) {
				logger.info("connect - message.getPayloadAsString ERROR");
			}
			result = operaterResend(triggerAlitripModels.getOuterId(), triggerAlitripModels.getItemId(),
					triggerAlitripModels.getAmount(), triggerAlitripModels.getToken(),
					triggerAlitripModels.getOuterIdSKU(), triggerAlitripModels.getMobile(), csvWriter);
			if (result) {
				message.setOutboundProperty("http.status", 200);
				body = "{\"code\":\"isv.success-all\", \"msg\":\"\"}";
			} else {
				CSVUtils.writeCsv(csvWriter, triggerAlitripModels.getOuterId(), "mule", "alitrip", "send",
						"ticket resend issued", "HTTP 500 - Internal Server Error");
				CSVUtils.sendError(triggerAlitripModels.getOuterId(), triggerAlitripModels.getItemId(),
						triggerAlitripModels.getOuterIdSKU(), triggerAlitripModels.getAmount(),
						triggerAlitripModels.getMobile(), "HTTP 500 - Internal Server Error",
						config.getDestinationEmailAddress(), config.getSourceEmailAddress(), config.getUserName(),
						config.getEmailPassord(), config.getMailSmtpHost(), config.getMailSmtpPort());
				message.setOutboundProperty("http.status", 500);
			}
			CSVUtils.writeCsv(csvWriter, triggerAlitripModels.getOuterId(), "mule", "alitrip", "response",
					"ticket resend trigger", body);
			break;
		case "modifyMobile":
			try {
				CSVUtils.writeCsv(csvWriter, triggerAlitripModels.getOuterId(), "alitrip", "mule", "send",
						"modify mobile trigger", message.getPayloadAsString());
			} catch (Exception e) {
				logger.info("connect - message.getPayloadAsString ERROR");
			}
			result = operaterModifyMobile(triggerAlitripModels.getOuterId(), triggerAlitripModels.getItemId(),
					triggerAlitripModels.getAmount(), triggerAlitripModels.getToken(),
					triggerAlitripModels.getOuterIdSKU(), triggerAlitripModels.getMobile(), csvWriter);
			if (result) {
				message.setOutboundProperty("http.status", 200);
				body = "{\"code\":\"isv.success-all\", \"msg\":\"\"}";
			} else {
				CSVUtils.writeCsv(csvWriter, triggerAlitripModels.getOuterId(), "mule", "alitrip", "send",
						"modify mobile issued", "HTTP 500 - Internal Server Error");
				CSVUtils.sendError(triggerAlitripModels.getOuterId(), triggerAlitripModels.getItemId(),
						triggerAlitripModels.getOuterIdSKU(), triggerAlitripModels.getAmount(),
						triggerAlitripModels.getMobile(), "HTTP 500 - Internal Server Error",
						config.getDestinationEmailAddress(), config.getSourceEmailAddress(), config.getUserName(),
						config.getEmailPassord(), config.getMailSmtpHost(), config.getMailSmtpPort());
				message.setOutboundProperty("http.status", 500);
			}
			CSVUtils.writeCsv(csvWriter, triggerAlitripModels.getOuterId(), "mule", "alitrip", "response",
					"modify mobile trigger", body);
			break;
		case "refundSuccess":
			body = "{\"code\":\"isv.success-all\", \"msg\":\"\"}";
			break;
		case "heartBeatCheck":
			// String ipHost = "";
			// try {
			// ipHost = InetAddress.getLocalHost().getHostAddress();
			// } catch (Exception e) {
			// CSVUtils.writeCsv(csvWriter, triggerAlitripModels.getOuterId(),
			// "mule", "alitrip", "send",
			// "heartBeatCheck issued", e.getMessage());
			// CSVUtils.sendError(triggerAlitripModels.getOuterId(),
			// triggerAlitripModels.getItemId(),
			// triggerAlitripModels.getOuterIdSKU(),
			// triggerAlitripModels.getAmount(),
			// triggerAlitripModels.getMobile(), e.getMessage(),
			// config.getDestinationEmailAddress(),
			// config.getSourceEmailAddress(), config.getUserName(),
			// config.getEmailPassord(),
			// config.getMailSmtpHost(), config.getMailSmtpPort());
			// }
			String ipHost = "";
			try {
				Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
				while (e.hasMoreElements()) {
					NetworkInterface n = (NetworkInterface) e.nextElement();
					Enumeration<InetAddress> ee = n.getInetAddresses();
					while (ee.hasMoreElements()) {
						InetAddress i = (InetAddress) ee.nextElement();
						if (!i.isLoopbackAddress() && !i.equals(InetAddress.getLocalHost().getHostAddress())) {
							ipHost = (i.getHostAddress());
						}
						logger.error("HOST IP " + i + ": " + i.getHostAddress());
					}
				}

			} catch (Exception e) {
				CSVUtils.writeCsv(csvWriter, triggerAlitripModels.getOuterId(), "mule", "alitrip", "send",
						"heartBeatCheck issued", e.getMessage());
				CSVUtils.sendError(triggerAlitripModels.getOuterId(), triggerAlitripModels.getItemId(),
						triggerAlitripModels.getOuterIdSKU(), triggerAlitripModels.getAmount(),
						triggerAlitripModels.getMobile(), e.getMessage(), config.getDestinationEmailAddress(),
						config.getSourceEmailAddress(), config.getUserName(), config.getEmailPassord(),
						config.getMailSmtpHost(), config.getMailSmtpPort());
				logger.error("heartbeatCheck ERROR: " + e.getMessage());
			}
			body = "{\"code\":\"isv.success-all\", \"msg\":\"" + ipHost + "\"}";
			break;
		default:
			try {
				CSVUtils.writeCsv(csvWriter, triggerAlitripModels.getOuterId(), "mule", "alitrip", "send",
						"Unknown trigger", message.getPayloadAsString());
				CSVUtils.sendError(triggerAlitripModels.getOuterId(), triggerAlitripModels.getItemId(),
						triggerAlitripModels.getOuterIdSKU(), triggerAlitripModels.getAmount(),
						triggerAlitripModels.getMobile(), message.getPayloadAsString(),
						config.getDestinationEmailAddress(), config.getSourceEmailAddress(), config.getUserName(),
						config.getEmailPassord(), config.getMailSmtpHost(), config.getMailSmtpPort());
				logger.info("Unknown trigger: " + message.getPayloadAsString());
			} catch (Exception e) {
				CSVUtils.writeCsv(csvWriter, triggerAlitripModels.getOuterId(), "mule", "alitrip", "send",
						"Unknown trigger ERROR: ", e.getMessage());
				CSVUtils.sendError(triggerAlitripModels.getOuterId(), triggerAlitripModels.getItemId(),
						triggerAlitripModels.getOuterIdSKU(), triggerAlitripModels.getAmount(),
						triggerAlitripModels.getMobile(), e.getMessage(), config.getDestinationEmailAddress(),
						config.getSourceEmailAddress(), config.getUserName(), config.getEmailPassord(),
						config.getMailSmtpHost(), config.getMailSmtpPort());
				logger.info("Unknown trigger ERROR: " + e.getMessage());
			} // send mail
			break;
		}

		CSVUtils.writeCsv(csvWriter);
		CSVUtils.endCsv(csvWriter);
		// CSVUtils.monthlySend(config.getDestinationEmailAddress(),
		// config.getSourceEmailAddress(), config.getUserName(),
		// config.getEmailPassord(), config.getMailSmtpHost(),
		// config.getMailSmtpPort());
		CSVUtils.anualSendExpiredAlarm(config.getDestinationEmailAddress(), config.getSourceEmailAddress(),
				config.getUserName(), config.getEmailPassord(), config.getMailSmtpHost(), config.getMailSmtpPort());

		return body;
	}

	public ConnectorConfig getConfig() {
		return config;
	}

	public void setConfig(ConnectorConfig config) {
		this.config = config;
	}

	/**
	 * CONSTRUCTORS
	 */

	public void InitiatingServices() {
		if (egalaxyServices == null) {
			egalaxyServices = new EgalaxyServices();
			egalaxyServices.setUrl(config.getEgalaxyUrl());
			egalaxyServices.setCustomerId(config.getEgalaxyCustomerId());
			egalaxyServices.setSalesProgram(config.getEgalaxySalesProgram());
			egalaxyServices.setSourceId(config.getEgalaxySourceId());
			egalaxyServices.setOrderStatus(config.getEgalaxyOrderStatus());
			egalaxyServices.setDeliveryMethod(config.getEgalaxyDeliveryMethod());
			egalaxyServices.setTimeRetry(config.getAlitripTimeRetry());
		}
		if (alitripServices == null) {
			alitripServices = new AlitripServices();
			alitripServices.setUrl(config.getAlitripUrl());
			alitripServices.setAppKey(config.getAlitripAppKey());
			alitripServices.setSecret(config.getAlitripSecret());
			alitripServices.setSessionKey(config.getAlitripSessionKey());
			alitripServices.setTimeRetry(config.getAlitripTimeRetry());
			alitripServices.setConsumeTimeRetry(config.getAlitripConsumeTimeRetry());
		}

		if (csvWriter == null) {
			csvWriter = CSVUtils.beginCsvFile();
		}

		if (database == null) {
			try {
				database = new Database(config.getDestinationEmailAddress(), config.getSourceEmailAddress(),
						config.getUserName(), config.getEmailPassord(), config.getMailSmtpHost(),
						config.getMailSmtpPort(), csvWriter);
				database.setMysqlHost(config.getMysqlHost());
				database.setMysqlDatabase(config.getMysqlDatabase());
				database.setMysqlUser(config.getMysqlUser());
				database.setMysqlPasswork(config.getMysqlPassword());
				database.setPeriodicConsume(config.getPeriodicConsume());
				database.setMyTable(config.getMysqlTable());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (triggerAlitripModels == null) {
			triggerAlitripModels = new TriggerAlitripModels();
			triggerAlitripModels.setSecret(config.getSecret());
		}

	}

	/**
	 * SERVICE FUNCTIONS
	 */

	private boolean operaterSend(String outerId, String itemId, String timestamp, String amount, String token,
			String plu, String detailType, String ticketDate, String expirationDate, String mobile,
			FileChannel csvWriter) {

		logger.info(outerId + " - operaterSend - START");
		boolean result = false;
		// handle order already exist
		egalaxyServices.sendCreateOrderRequest(outerId, itemId, timestamp, amount, plu, detailType, expirationDate,
				mobile, config.getDestinationEmailAddress(), config.getSourceEmailAddress(), config.getUserName(),
				config.getEmailPassord(), config.getMailSmtpHost(), config.getMailSmtpPort(), csvWriter);
		QueryOrderResponseModel queryOrderResponseModel = egalaxyServices.sendQueryOrderRequest(outerId, itemId, plu,
				mobile, config.getDestinationEmailAddress(), config.getSourceEmailAddress(), config.getUserName(),
				config.getEmailPassord(), config.getMailSmtpHost(), config.getMailSmtpPort(), csvWriter);
		List<String> visualIds = queryOrderResponseModel.getVisualIds();
		if (visualIds.size() > 0) {
			boolean generateBarcodeImagesResult = alitripServices.generateBarcodeImages(outerId, itemId, amount, plu,
					mobile, config.getDestinationEmailAddress(), config.getSourceEmailAddress(), config.getUserName(),
					config.getEmailPassord(), config.getMailSmtpHost(), config.getMailSmtpPort(), visualIds, csvWriter);
			if (generateBarcodeImagesResult) {
				boolean mergeImagesResult = alitripServices.mergeImages(outerId, itemId, amount, plu, mobile,
						config.getDestinationEmailAddress(), config.getSourceEmailAddress(), config.getUserName(),
						config.getEmailPassord(), config.getMailSmtpHost(), config.getMailSmtpPort(), visualIds,
						csvWriter);
				if (mergeImagesResult) {
					String qrImage = alitripServices.uploadBarcodeImage(outerId, itemId, amount, plu, mobile,
							config.getDestinationEmailAddress(), config.getSourceEmailAddress(), config.getUserName(),
							config.getEmailPassord(), config.getMailSmtpHost(), config.getMailSmtpPort(), csvWriter);
					if (qrImage != null) {
						result = alitripServices.maSend(outerId, itemId, visualIds, qrImage, amount, token, plu, mobile,
								config.getDestinationEmailAddress(), config.getSourceEmailAddress(),
								config.getUserName(), config.getEmailPassord(), config.getMailSmtpHost(),
								config.getMailSmtpPort(), csvWriter);
					} else {
						CSVUtils.writeCsv(csvWriter, outerId, "mule", "alitrip", "send", "new ticket issued",
								"Sending operation - ERROR - qrImage is null");
						CSVUtils.sendError(outerId, itemId, plu, amount, mobile,
								"Sending operation - ERROR - qrImage is null", config.getDestinationEmailAddress(),
								config.getSourceEmailAddress(), config.getUserName(), config.getEmailPassord(),
								config.getMailSmtpHost(), config.getMailSmtpPort());
						logger.info(outerId + " - operaterSend - ERROR - qrImage is null");
					}
				} else {
					CSVUtils.writeCsv(csvWriter, outerId, "mule", "alitrip", "send", "new ticket issued",
							"Sending operation - ERROR - mergeImagesResult is Failed");
					CSVUtils.sendError(outerId, itemId, plu, amount, mobile,
							"Sending operation - ERROR - mergeImagesResult is Failed",
							config.getDestinationEmailAddress(), config.getSourceEmailAddress(), config.getUserName(),
							config.getEmailPassord(), config.getMailSmtpHost(), config.getMailSmtpPort());
					logger.info(outerId + " - operaterSend - ERROR - mergeImagesResult is Failed");
				}
			} else {
				CSVUtils.writeCsv(csvWriter, outerId, "mule", "alitrip", "send", "new ticket issued",
						"Sending operation - ERROR - generateBarcodeImagesResult is Failed");
				CSVUtils.sendError(outerId, itemId, plu, amount, mobile,
						"Sending operation - ERROR - generateBarcodeImagesResult is Failed",
						config.getDestinationEmailAddress(), config.getSourceEmailAddress(), config.getUserName(),
						config.getEmailPassord(), config.getMailSmtpHost(), config.getMailSmtpPort());
				logger.info(outerId + " - operaterSend - ERROR - generateBarcodeImagesResult is Failed");
			}
		} else {
			CSVUtils.writeCsv(csvWriter, outerId, "mule", "alitrip", "send", "new ticket issued",
					"Sending operation - ERROR - visualIds is null");
			CSVUtils.sendError(outerId, itemId, plu, amount, mobile, "Sending operation - ERROR - visualIds is null",
					config.getDestinationEmailAddress(), config.getSourceEmailAddress(), config.getUserName(),
					config.getEmailPassord(), config.getMailSmtpHost(), config.getMailSmtpPort());
			logger.info(outerId + " - operaterSend - ERROR - visualIds is null");
		}

		logger.info(outerId + " - operaterSend - END");

		return result;
	}

	private boolean operaterResend(String outerId, String itemId, String amount, String token, String plu,
			String mobile, FileChannel csvWriter) {

		logger.info(outerId + " - operaterResend - START");
		boolean result = false;
		QueryOrderResponseModel queryOrderResponseModel = egalaxyServices.sendQueryOrderRequest(outerId, itemId, plu,
				mobile, config.getDestinationEmailAddress(), config.getSourceEmailAddress(), config.getUserName(),
				config.getEmailPassord(), config.getMailSmtpHost(), config.getMailSmtpPort(), csvWriter);
		List<String> visualIds = queryOrderResponseModel.getVisualIds();
		if (visualIds.size() > 0) {
			boolean generateBarcodeImagesResult = alitripServices.generateBarcodeImages(outerId, itemId, amount, plu,
					mobile, config.getDestinationEmailAddress(), config.getSourceEmailAddress(), config.getUserName(),
					config.getEmailPassord(), config.getMailSmtpHost(), config.getMailSmtpPort(), visualIds, csvWriter);
			if (generateBarcodeImagesResult) {
				boolean mergeImagesResult = alitripServices.mergeImages(outerId, itemId, amount, plu, mobile,
						config.getDestinationEmailAddress(), config.getSourceEmailAddress(), config.getUserName(),
						config.getEmailPassord(), config.getMailSmtpHost(), config.getMailSmtpPort(), visualIds,
						csvWriter);
				if (mergeImagesResult) {
					String qrImage = alitripServices.uploadBarcodeImage(outerId, itemId, amount, plu, mobile,
							config.getDestinationEmailAddress(), config.getSourceEmailAddress(), config.getUserName(),
							config.getEmailPassord(), config.getMailSmtpHost(), config.getMailSmtpPort(), csvWriter);
					if (qrImage != null) {
						result = alitripServices.reSend(outerId, itemId, visualIds, qrImage, amount, token, plu, mobile,
								config.getDestinationEmailAddress(), config.getSourceEmailAddress(),
								config.getUserName(), config.getEmailPassord(), config.getMailSmtpHost(),
								config.getMailSmtpPort(), csvWriter);
					} else {
						CSVUtils.writeCsv(csvWriter, outerId, "mule", "alitrip", "send", "resend ticket issued",
								"Resend operation - ERROR - qrImage is null");
						CSVUtils.sendError(outerId, itemId, plu, amount, mobile,
								"Resend operation - ERROR - qrImage is null", config.getDestinationEmailAddress(),
								config.getSourceEmailAddress(), config.getUserName(), config.getEmailPassord(),
								config.getMailSmtpHost(), config.getMailSmtpPort());
						logger.info(outerId + " - operaterResend - ERROR - qrImage is null");
					}
				} else {
					CSVUtils.writeCsv(csvWriter, outerId, "mule", "alitrip", "send", "resend ticket issued",
							"Sending operation - ERROR - mergeImagesResult is Failed");
					CSVUtils.sendError(outerId, itemId, plu, amount, mobile,
							"Resend operation - ERROR - mergeImagesResult is Failed",
							config.getDestinationEmailAddress(), config.getSourceEmailAddress(), config.getUserName(),
							config.getEmailPassord(), config.getMailSmtpHost(), config.getMailSmtpPort());
					logger.info(outerId + " - operaterResend - ERROR - mergeImagesResult is failed");
				}
			} else {
				CSVUtils.writeCsv(csvWriter, outerId, "mule", "alitrip", "send", "resend ticket issued",
						"Resend operation - ERROR - generateBarcodeImagesResult is Failed");
				CSVUtils.sendError(outerId, itemId, plu, amount, mobile,
						"Resend operation - ERROR - generateBarcodeImagesResult is Failed",
						config.getDestinationEmailAddress(), config.getSourceEmailAddress(), config.getUserName(),
						config.getEmailPassord(), config.getMailSmtpHost(), config.getMailSmtpPort());
				logger.info(outerId + " - operaterResend - ERROR - generateBarcodeImagesResult is failed");
			}
		} else {
			CSVUtils.writeCsv(csvWriter, outerId, "mule", "alitrip", "send", "resend ticket issued",
					"Resend operation - ERROR - visualIds is null");
			CSVUtils.sendError(outerId, itemId, plu, amount, mobile, "Resend operation - ERROR - visualIds is null",
					config.getDestinationEmailAddress(), config.getSourceEmailAddress(), config.getUserName(),
					config.getEmailPassord(), config.getMailSmtpHost(), config.getMailSmtpPort());
			logger.info(outerId + " - operaterResend - ERROR - visualIds is null");
		}
		logger.info(outerId + " - operaterResend - END");
		return result;
	}

	private boolean operaterModifyMobile(String outerId, String itemId, String amount, String token, String plu,
			String mobile, FileChannel csvWriter) {

		logger.info(outerId + " - operaterModifyMobile - START");
		boolean result = false;
		// update contact here, before query order
		boolean isSendUpdateContactRequest = egalaxyServices.sendUpdateContactRequest(outerId, itemId, amount, plu,
				mobile, config.getDestinationEmailAddress(), config.getSourceEmailAddress(), config.getUserName(),
				config.getEmailPassord(), config.getMailSmtpHost(), config.getMailSmtpPort(), csvWriter);

		if (isSendUpdateContactRequest) {
			QueryOrderResponseModel queryOrderResponseModel = egalaxyServices.sendQueryOrderRequest(outerId, itemId,
					plu, mobile, config.getDestinationEmailAddress(), config.getSourceEmailAddress(),
					config.getUserName(), config.getEmailPassord(), config.getMailSmtpHost(), config.getMailSmtpPort(),
					csvWriter);
			List<String> visualIds = queryOrderResponseModel.getVisualIds();
			if (visualIds.size() > 0) {
				boolean generateBarcodeImagesResult = alitripServices.generateBarcodeImages(outerId, itemId, amount,
						plu, mobile, config.getDestinationEmailAddress(), config.getSourceEmailAddress(),
						config.getUserName(), config.getEmailPassord(), config.getMailSmtpHost(),
						config.getMailSmtpPort(), visualIds, csvWriter);
				if (generateBarcodeImagesResult) {
					boolean mergeImagesResult = alitripServices.mergeImages(outerId, itemId, amount, plu, mobile,
							config.getDestinationEmailAddress(), config.getSourceEmailAddress(), config.getUserName(),
							config.getEmailPassord(), config.getMailSmtpHost(), config.getMailSmtpPort(), visualIds,
							csvWriter);
					if (mergeImagesResult) {

						String qrImage = alitripServices.uploadBarcodeImage(outerId, itemId, amount, plu, mobile,
								config.getDestinationEmailAddress(), config.getSourceEmailAddress(),
								config.getUserName(), config.getEmailPassord(), config.getMailSmtpHost(),
								config.getMailSmtpPort(), csvWriter);
						if (qrImage != null) {
							result = alitripServices.reSend(outerId, itemId, visualIds, qrImage, amount, token, plu,
									mobile, config.getDestinationEmailAddress(), config.getSourceEmailAddress(),
									config.getUserName(), config.getEmailPassord(), config.getMailSmtpHost(),
									config.getMailSmtpPort(), csvWriter);
						} else {
							CSVUtils.writeCsv(csvWriter, outerId, "mule", "alitrip", "send", "modify mobile issued",
									"Modify mobile operation - ERROR - qrImage is null");
							CSVUtils.sendError(outerId, itemId, plu, amount, mobile,
									"Modify mobile operation - ERROR - qrImage is null",
									config.getDestinationEmailAddress(), config.getSourceEmailAddress(),
									config.getUserName(), config.getEmailPassord(), config.getMailSmtpHost(),
									config.getMailSmtpPort());
							logger.info(outerId + " - operaterModifyMobile - ERROR - qrImage is null");
						}

					} else {
						CSVUtils.writeCsv(csvWriter, outerId, "mule", "alitrip", "send", "modify mobile ticket issued",
								"Modify mobile operation - ERROR - mergeImagesResult is Failed");
						CSVUtils.sendError(outerId, itemId, plu, amount, mobile,
								"Modify mobile operation - ERROR - mergeImagesResult is Failed",
								config.getDestinationEmailAddress(), config.getSourceEmailAddress(),
								config.getUserName(), config.getEmailPassord(), config.getMailSmtpHost(),
								config.getMailSmtpPort());
						logger.info(outerId + " - operaterModifyMobile - ERROR - mergeImagesResult is false");
					}
				} else {
					CSVUtils.writeCsv(csvWriter, outerId, "mule", "alitrip", "send", "modify mobile issued",
							"Modify mobile operation - ERROR - generateBarcodeImagesResult is Failed");
					CSVUtils.sendError(outerId, itemId, plu, amount, mobile,
							"Modify mobile operation - ERROR - generateBarcodeImagesResult is Failed",
							config.getDestinationEmailAddress(), config.getSourceEmailAddress(), config.getUserName(),
							config.getEmailPassord(), config.getMailSmtpHost(), config.getMailSmtpPort());
					logger.info(outerId + " - operaterModifyMobile - ERROR - generateBarcodeImagesResult is false");
				}
			} else {
				CSVUtils.writeCsv(csvWriter, outerId, "mule", "alitrip", "send", "modify mobile issued",
						"Modify mobile operation - ERROR - visualIds is null");
				CSVUtils.sendError(outerId, itemId, plu, amount, mobile,
						"Modify mobile operation - ERROR - visualIds is null", config.getDestinationEmailAddress(),
						config.getSourceEmailAddress(), config.getUserName(), config.getEmailPassord(),
						config.getMailSmtpHost(), config.getMailSmtpPort());
				logger.info(outerId + " - operaterModifyMobile - ERROR - visualIds is null");
			}
		} else {
			CSVUtils.writeCsv(csvWriter, outerId, "mule", "alitrip", "send", "modify mobile issued",
					"Modify mobile operation - ERROR - sendUpdateContactRequest is failed");
			CSVUtils.sendError(outerId, itemId, plu, amount, mobile,
					"Modify mobile operation - ERROR - sendUpdateContactRequest is failed",
					config.getDestinationEmailAddress(), config.getSourceEmailAddress(), config.getUserName(),
					config.getEmailPassord(), config.getMailSmtpHost(), config.getMailSmtpPort());
			logger.info(outerId + " - operaterModifyMobile - ERROR - sendUpdateContactRequest is false");
		}

		logger.info(outerId + " - operaterModifyMobile - END");
		return result;
	}

	public boolean operaterConsume(String outerId, String itemId, String plu, String mobile, FileChannel csvWriter) {
		// Query all outerId in database with status is "new" (ignore status
		// "consumed")
		// String outerId = "";
		logger.info(outerId + " - operaterConsume - START");
		boolean result = false;
		// send QueryOrder with outerId
		QueryOrderResponseModel queryOrderResponseModel = egalaxyServices.sendQueryOrderRequest(outerId, itemId, plu,
				mobile, config.getDestinationEmailAddress(), config.getSourceEmailAddress(), config.getUserName(),
				config.getEmailPassord(), config.getMailSmtpHost(), config.getMailSmtpPort(), csvWriter);
		List<String> visualIds = queryOrderResponseModel.getVisualIds();
		String num = queryOrderResponseModel.getQty();
		// run consume API
		if (visualIds.size() > 0) {
			result = alitripServices.consume(outerId, visualIds, num, config.getDestinationEmailAddress(),
					config.getSourceEmailAddress(), config.getUserName(), config.getUserName(),
					config.getMailSmtpHost(), config.getMailSmtpPort(), this.csvWriter);
		} else {
			CSVUtils.writeCsv(csvWriter, outerId, "mule", "alitrip", "send", "consume issued",
					"Consume operation - ERROR - visualIds is null");
			CSVUtils.sendError(outerId, itemId, plu, num, mobile, "Consume operation - ERROR - visualIds is null",
					config.getDestinationEmailAddress(), config.getSourceEmailAddress(), config.getUserName(),
					config.getUserName(), config.getMailSmtpHost(), config.getMailSmtpPort());
			logger.info(outerId + " - operaterConsume - ERROR - visualIds is null");
		}

		return result;
	}

}