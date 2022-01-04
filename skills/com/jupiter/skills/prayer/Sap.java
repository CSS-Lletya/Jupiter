package com.jupiter.skills.prayer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Sap {
	WARRIOR(Prayer.SAP_WARRIOR, 2214, 2215, 2216),
	RANGE(Prayer.SAP_RANGE, 2217, 2218, 2219),
	MAGE(Prayer.SAP_MAGE, 2220, 2221, 2222),
	SPIRIT(Prayer.SAP_SPIRIT, 2223, 2224, 2225);
	
	private Prayer prayer;

	private int spotAnimStart, projAnim, spotAnimHit;
}