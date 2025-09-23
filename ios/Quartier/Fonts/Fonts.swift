//
//  Untitled.swift
//  Quartier
//
//  Created by Emile Turcotte on 2025-07-13.
//

import SwiftUI

extension Font {
    static func titleBold(_ size: CGFloat) -> Font {
        .custom("Merriweather-Bold", size: size)
    }
    
    static func title(_ size: CGFloat) -> Font {
        .custom("Merriweather-Regular", size: size)
    }
    
    static func bodyBold(_ size: CGFloat) -> Font {
        .custom("MerriweatherSans-Bold", size: size)
    }
    
    static func body(_ size: CGFloat) -> Font {
        .custom("MerriweatherSans-Regular", size: size)
    }
}
