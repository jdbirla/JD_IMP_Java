package odm;

import static com.quipoz.COBOLFramework.COBOLFunctions.SPACES;
import static com.quipoz.COBOLFramework.COBOLFunctions.isEQ;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csc.smart.recordstructures.Varcom;
import com.csc.smart.utils.ListUtils;
import com.csc.zurich.common.ZurichConstants;
import com.csc.zurich.group.batchprograms.Bw978;
import com.csc.zurich.group.dataaccess.model.ZintMbrCampaignpf;
import com.csc.zurich.group.dataaccess.model.Zslppf;
import com.csc.zurich.group.dataaccess.model.Ztgmpf;
import com.csc.zurich.group.utils.ZNbImpVal;
import com.csc.zurich.interfaces.request.odm.ODMInsuranceRequest;
import com.csc.zurich.interfaces.request.odm.ODMRequest;
import com.csc.zurich.interfaces.response.odm.ODMResponse;
import com.quipoz.framework.util.AppVars;

public class Bw978Customized extends Bw978 {

	private static final Logger LOGGER = LoggerFactory.getLogger(Bw978Customized.class);

	List<ZintMbrCampaignpf> odmrespupdList = null;
	
	protected Bw978Customized setChunkSize(int c){
		this.chunckSize = c;
		return this;
	}
	protected int THREAD_NO=1;
	protected Bw978Customized setThreadNo(int t){
		this.THREAD_NO=t;
		return this;
	}
	
	@Override
	public void initialise1000() {
		LOGGER.debug("initialise1000 started.");
		preLoadEndorserData();
		preLoadItempfData();
		allMbrCpnDataFromTempThreadwise = zintMbrCampaignDAO
				.getFilteredNBRecords(null,THREAD_NO);
		LOGGER.debug("initialise1000 completed.");
	}

	@Override
	public void readFile2000() {
		LOGGER.debug("readFile2000 started.");
		chunkOfMbrCpnDataFromTemp = null;
		odmrespupdList = new ArrayList<>(ZurichConstants.DEFAULTSIZE);

		chunkOfMbrCpnDataFromTemp = allMbrCpnDataFromTempThreadwise
				.entrySet()
				.stream()
				.filter(x -> {
					return x.getValue().get(0).isChunkFlag() ? false : true;
				})
				.limit(chunckSize)
				.collect(
						Collectors
								.toMap(Map.Entry::getKey, Map.Entry::getValue));
		allMbrCpnDataFromTempThreadwise.entrySet().removeAll(
				chunkOfMbrCpnDataFromTemp.entrySet());
		LOGGER.debug("readFile2000 completed.");
	}
	
	public void update3000(){
		LOGGER.debug("update3000 started.");
		// If the chunkOfMbrCpnDataFromTemp is not having anything then do
		// NOTHING in update, so return asap.
		if (null == chunkOfMbrCpnDataFromTemp || chunkOfMbrCpnDataFromTemp.isEmpty()) {
		    return;
		}
		// to hold all the members of chunks in this cycle
		final List<ZintMbrCampaignpf> zintMbrCampaignpfListUpd = new ArrayList<>(6);

		for (final Map.Entry<String, List<ZintMbrCampaignpf>> entry : chunkOfMbrCpnDataFromTemp.entrySet()) {
		    ztgmpf = new Ztgmpf();
		    final ODMRequest odmRequest = new ODMRequest();
		    final List<ODMInsuranceRequest> insuranceList = new ArrayList<>(ZurichConstants.DEFAULTSIZE);
		    final List<ZintMbrCampaignpf> intmbrcamplist = entry.getValue();
		    String policyDataType = StringUtils.EMPTY;
		    String namedOrUnamed = StringUtils.EMPTY;
		    zwaveflg = StringUtils.EMPTY;
		    declinationFlag = false;
		    // the owner records.
		    boolean isRecordGood = true; // if error found in processing of
		    // any member.
		    final List<ZintMbrCampaignpf> tempMemDataList = new ArrayList<>(6); // at

		    if (ListUtils.isNotEmpty(intmbrcamplist)) {
			final Set<String> instypeSet = getUniqueZinsType(intmbrcamplist);

			// intmbrcamplist is sorted , so take the first record i.e. for
			// Owner and check
			// the uw reason field

			if (StringUtils.isNotBlank(intmbrcamplist.get(0).getReasunwritdeclination())) {
			    declinationFlag = true;
			}
			// ZJNPG-7389
			for (final ZintMbrCampaignpf intmbrcamp : intmbrcamplist) {

			    if (intmbrcamp.getCltind().equalsIgnoreCase(ZurichConstants.CLIENT_IND_INSURED)
				    && null != intmbrcamp.getInsuredrole()
				    && intmbrcamp.getInsuredrole().equalsIgnoreCase(ZurichConstants.MAININSURED)) {
				namedOrUnamed = intmbrcamp.getZslptyp();
				// Set ODM call type
				if (null != intmbrcamp.getProdcat()) {
				    odmRequest.setOdmCallType(intmbrcamp.getProdcat());
				}
			    }
			}

			for (final ZintMbrCampaignpf intmbrcamp : intmbrcamplist) {
			    // for the processed chunk set the flag, so that it will
			    // not be picked in next loop.
			    intmbrcamp.setChunkFlag(ZurichConstants.TRUE);
			    if (intmbrcamp.getCltind().equalsIgnoreCase(ZurichConstants.CLIENT_IND_OWNER)) {
				setODMRequestHeader(intmbrcamp, odmRequest);
				policyDataType = intmbrcamp.getDatatype();
				zwaveflg = intmbrcamp.getZwavgflg();
			    }

			}
			// Set insurance request
			final Map<String, List<Zslppf>> ZslppfMap = getZslpppfMapBasedinsRole(intmbrcamplist);
			for (final String zinstype : instypeSet) {
			    final ODMInsuranceRequest insurancerequest = new ODMInsuranceRequest();
			    insurancerequest.setInsuranceType(zinstype);

			    // Set Rider Code list for every insurance type
			    insurancerequest.setRiderCodeList(getRiderCodeListBasedonInsType(ZslppfMap, zinstype));

			    // Set Insured list for every insurance type
			    if (StringUtils.isNotEmpty(namedOrUnamed)
				    && namedOrUnamed.equalsIgnoreCase(ZurichConstants.UNNAMED)) {
				insurancerequest.setInsuredList(getInsuredListBasedonZinsTypeUnamed(intmbrcamplist, ZslppfMap,
					zinstype, odmRequest.getInceptionDate()));
			    } else {

				insurancerequest.setInsuredList(getInsuredListBasedonZinsTypeNamed(intmbrcamplist, ZslppfMap,
					zinstype, odmRequest.getInceptionDate()));

			    }
			    insuranceList.add(insurancerequest);
			}

		    }
		    odmRequest.setInsuranceList(insuranceList);
		    ODMResponse odmResponse = null;
		    odmResponse = getODMResponse(odmRequest, intmbrcamplist);
		    
		    
		    
		    // Perform validations on ODM response.
		    boolean isValidationNotOk = false;
		    if (null != odmResponse) {
//			isValidationNotOk = validateOdmResponse(intmbrcamplist, odmResponse, namedOrUnamed, policyDataType);
		    } else {
			isValidationNotOk = true;
		    }

		    if (null != odmResponse && !isValidationNotOk) {

			for (final ZintMbrCampaignpf intmbrcamp : intmbrcamplist) {
			    if (intmbrcamp.getCltind().equalsIgnoreCase(ZurichConstants.CLIENT_IND_OWNER)) {
				intmbrcamp.setOdmresp(odmResponse);
				odmrespupdList.add(intmbrcamp);
			    }
			    if (null != intmbrcamp.getDatatype() && isEQ(intmbrcamp.getDatatype(), 3)
				    && intmbrcamp.getCltind().equalsIgnoreCase(ZurichConstants.CLIENT_IND_INSURED)) {
				odmrespupdList.add(intmbrcamp);
			    }
			    // log the successful completion of odm process.
//			    callCntrlTotal(ZurichConstants.CT02, 1);

			    // add the cooked member information in the temporary
			    // list.
			    tempMemDataList.add(intmbrcamp);
			}
		    } else {
			// log the count of records for which odm process had
			// errors.
//			callCntrlTotal(ZurichConstants.CT03, 1);
			// as it is error case, mark this policy as wrong. it
			// will be recorded in DB.
			isRecordGood = false;
			tempMemDataList.addAll(intmbrcamplist);
		    }

		    // In the above loop-processing if there has been error for
		    // "any" member, then, all of the member shall be
		    // marked as error.
		    if (isRecordGood) { // if not a single error=all success.
			tempMemDataList.forEach(item -> {
			    item.setIsodmcmp(ZurichConstants.Y);
			    zintMbrCampaignpfListUpd.add(item);
			});

		    } else { // if error for any member, has occurred.
			tempMemDataList.forEach(item -> {
			    item.setIsodmcmp(ZurichConstants.N);
			    zintMbrCampaignpfListUpd.add(item);
			});
		    }
		}
//		appVars.commit();
		LOGGER.debug("update3000 completed.");
	}

}
