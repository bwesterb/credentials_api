/**
 * Test.java
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright (C) Pim Vullers, Radboud University Nijmegen, May 2012,
 * Copyright (C) Wouter Lueks, Radboud University Nijmegen, July 2012.
 */

package org.irmacard.credentials.idemix.test;

import java.io.File;
import java.math.BigInteger;
import java.net.URI;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.smartcardio.TerminalFactory;

import net.sourceforge.scuba.smartcards.TerminalCardService;
import net.sourceforge.scuba.smartcards.WrappingCardService;
import net.sourceforge.scuba.util.Hex;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.irmacard.credentials.util.CardHolderVerificationService;
import org.irmacard.credentials.util.SecureMessagingWrapper;

import service.IdemixService;

import com.ibm.zurich.credsystem.utils.Locations;
import com.ibm.zurich.idmx.dm.Values;
import com.ibm.zurich.idmx.issuance.IssuanceSpec;
import com.ibm.zurich.idmx.issuance.Issuer;
import com.ibm.zurich.idmx.issuance.Message;
import com.ibm.zurich.idmx.key.IssuerKeyPair;

public class Test {

    static { Security.addProvider(new BouncyCastleProvider()); }
    
    private static final IvParameterSpec ZERO_IV_PARAM_SPEC = 
			new IvParameterSpec(new byte[8]);
    
	public static void main(String[] args) {
		try {
			Mac mac = Mac.getInstance("DESEDEMAC64WITHISO7816-4PADDING");
			Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");
			byte[] key = new byte[16];//{1,2,3,4,5,6,7,8,1,(byte)0x80,0,0,0,0,0,0};
			SecretKey ksMac = new SecretKeySpec(key, "DESEDE");
			mac.init(ksMac);
			byte[] in = new byte[]{ 0x02 };
			byte[] out = mac.doFinal(in);
			System.out.println(Hex.toHexString(out));
			byte[] in2 = new byte[]{1,2,3,4,5,6,7,8,1,(byte)0x80,0,0,0,0,0,0};
			cipher.init(Cipher.ENCRYPT_MODE, ksMac, ZERO_IV_PARAM_SPEC);
			byte[] out2 = cipher.doFinal(in2);
			System.out.println(Hex.toHexString(out2));
			
			String credStruct = "studentCard";
			URI BASE_LOCATION = new File(System.getProperty("user.dir")).toURI().resolve("irma_configuration/RU/");
			System.out.println("got base");
	        URI CRED_LOCATION = BASE_LOCATION.resolve("Issues/structure.xml");
	        System.out.println("got cred");
	        URI ISSUER_LOCATION = BASE_LOCATION.resolve("ipk.xml");
	        System.out.println("got issuer");
	        URI credStructId = new URI("http://www.ngo.org/" + credStruct + ".xml");
	        System.out.println("got struct");
	        // loading credential structure linked to a URI
	        Locations.init(credStructId, CRED_LOCATION);
	        System.out.println("got init");
	        // create the issuance specification
	        IssuanceSpec issuanceSpec = new IssuanceSpec(ISSUER_LOCATION, credStructId);
	        System.out.println("got spec");
	        URI iskLocation = BASE_LOCATION.resolve("private/isk.xml");
	        URI ipkLocation = ISSUER_LOCATION.resolve("ipk.xml");
	        URI BASE_ID = new URI("http://www.irmacard.org/credentials/phase1/RU/");
	        URI ISSUER_ID = new URI("http://www.issuer.com/");
	        IssuerKeyPair issuerKey = Locations.initIssuer(BASE_LOCATION, BASE_ID.toString(),
	                iskLocation, ipkLocation, ISSUER_ID.resolve("ipk.xml"));
	        short CRED_NR = (short) 4;

			Values values = new Values(issuerKey.getPublicKey().getGroupParams().getSystemParams());
	        System.out.println("got val");
	        values.add("attr1", BigInteger.valueOf(1313));
	        values.add("attr2", BigInteger.valueOf(1314));
	        values.add("attr3", BigInteger.valueOf(1315));
	        values.add("attr4", BigInteger.valueOf(1316));
			System.out.println("test");
			
			// Terminal for communication with the actual card
            TerminalCardService terminal = new TerminalCardService(TerminalFactory.getDefault().terminals().list().get(0));
            CardHolderVerificationService pinpad = new CardHolderVerificationService(terminal);
            
            // Wrapper which performs secure messaging
			SecureMessagingWrapper sm = new SecureMessagingWrapper(ksMac, ksMac);
			
			// Turn the terminal in a wrapping enabled service, wrapping is disabled by default, enable using wrapper.enable() 
			WrappingCardService wrapper = new WrappingCardService(pinpad, sm);
			
			// Finally make an idemix service out of all this.
			IdemixService service = new IdemixService(wrapper, CRED_NR);

			// Some tests
            service.open();
            //wrapper.enable();
            service.generateMasterSecret();
            int tries = pinpad.verifyPIN();
            if (tries != CardHolderVerificationService.PIN_OK) {
            	System.err.println("PIN verification failed. Tries left: " + tries);
            }
            service.setIssuanceSpecification(issuanceSpec);
            service.setAttributes(issuanceSpec, values);
            
            Issuer issuer = new Issuer(issuerKey, issuanceSpec, null, null, values);
            Message msgToRecipient1 = issuer.round0();
            Message msgToIssuer1 = service.round1(msgToRecipient1);
            Message msgToRecipient2 = issuer.round2(msgToIssuer1);
            service.round3(msgToRecipient2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
