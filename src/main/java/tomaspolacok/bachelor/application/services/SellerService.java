package tomaspolacok.bachelor.application.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tomaspolacok.bachelor.application.entities.EbaySeller;
import tomaspolacok.bachelor.application.repositories.EbaySellerRepository;

@Service
public class SellerService {

	
	@Autowired
	EbaySellerRepository sellerRepository;
	
	/**
	 * Returns seller by his name or throws an exception if it doesn't exist
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public EbaySeller getSeller(String name) throws Exception {
		EbaySeller seller = sellerRepository.getOne(name);
		if (seller == null) throw new Exception();
		return seller;
	}
}
