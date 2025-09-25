//
//  Placeholder.swift
//  Quartier
//
//  Created by Emile Turcotte on 2025-09-24.
//

import SwiftUI

struct Placeholder : View {
    var title: String
    var bodyText: String
    var iconName: String
    
    var buttonIconName: String? = nil
    var buttonText: String? = nil
    var buttonAction: (() -> Void)? = nil
    
    var body: some View {
        HStack {
            VStack(alignment: .leading) {
                Text(title).font(.bodyBold(17))
                Text(bodyText).font(.body(17))
                
                if let buttonText, let buttonIconName, let buttonAction {
                    Button(action: buttonAction) {
                        HStack {
                            Spacer()
                            Image(systemName: buttonIconName)
                                .frame(width: 30, height: 30)
                            Text(buttonText)
                            Spacer()
                        }
                    }.buttonStyle(.borderedProminent)
                }
            }
            Spacer()
            Image(systemName: iconName).resizable().padding().frame(width: 128, height: 128).foregroundStyle(.accent)
        }.padding(20)
    }
}

#Preview {
    Placeholder(title: "Title", bodyText: "Body", iconName: "face.smiling", buttonIconName: "chevron.up", buttonText: "Add more", buttonAction: {})
}
