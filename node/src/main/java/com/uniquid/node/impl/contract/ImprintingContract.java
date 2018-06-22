package com.uniquid.node.impl.contract;

import java.util.List;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.uniquid.node.impl.UniquidNodeStateContext;
import com.uniquid.register.exception.RegisterException;
import com.uniquid.register.provider.ProviderChannel;
import com.uniquid.register.provider.ProviderRegister;

/**
 * Class that manage imprinting contracts
 * 
 * @author giuseppe
 *
 */
@SuppressWarnings("rawtypes")
public class ImprintingContract extends AbstractContract {
	
	public static final String CONTRACT_FUNCTION = "00000000400000000000000000000000000000";

	private static final Logger LOGGER = LoggerFactory.getLogger(ImprintingContract.class);

	@SuppressWarnings("unchecked")
	public ImprintingContract(UniquidNodeStateContext uniquidNodeStateContext) {
		super(uniquidNodeStateContext);
	}
	
	@Override
	public void doRealContract(final Transaction tx) throws Exception {
		
		LOGGER.info("Making imprint contract from TX {}", tx.getHashAsString());

		// Retrieve sender
		String sender = tx.getInput(0).getFromAddress().toBase58();
		
		// Check output
		List<TransactionOutput> transactionOutputs = tx.getOutputs();
		for (TransactionOutput to : transactionOutputs) {

			Address address = to.getAddressFromP2PKHScript(uniquidNodeStateContext.getUniquidNodeConfiguration().getNetworkParameters());
			if (address != null && address.equals(uniquidNodeStateContext.getImprintingAddress())) {

				// This is our imprinter!!!
				
				LOGGER.info("Received imprint contract from {}!", sender);

				ProviderRegister providerRegister = uniquidNodeStateContext.getUniquidNodeConfiguration().getRegisterFactory().getProviderRegister();

				// Create provider channel
				final ProviderChannel providerChannel = new ProviderChannel();
				providerChannel.setUserAddress(sender);
				providerChannel.setProviderAddress(uniquidNodeStateContext.getImprintingAddress().toBase58());
				providerChannel.setBitmask(CONTRACT_FUNCTION);
				providerChannel.setRevokeAddress("IMPRINTING");
				providerChannel.setRevokeTxId(tx.getHashAsString());
				providerChannel.setCreationTime(tx.getUpdateTime().getTime()/1000);

				uniquidNodeStateContext.getUniquidNodeConfiguration().getRegisterFactory().getTransactionManager().startTransaction();
				
				try {
					
					// persist channel
					providerRegister.insertChannel(providerChannel);
					
					uniquidNodeStateContext.getUniquidNodeConfiguration().getRegisterFactory().getTransactionManager().commitTransaction();
				
				} catch (RegisterException ex) {
					
					LOGGER.error("Error while inserting channel", ex);
					
					uniquidNodeStateContext.getUniquidNodeConfiguration().getRegisterFactory().getTransactionManager().rollbackTransaction();
					
					// ReThrow
					throw ex;
					
				}

				// send event
				uniquidNodeStateContext.getUniquidNodeEventService().onProviderContractCreated(providerChannel);
				
				break;

			}

		}

	}

	@Override
	public void revokeRealContract(final Transaction tx) throws Exception {
		// DO NOTHING
	}
	
}
