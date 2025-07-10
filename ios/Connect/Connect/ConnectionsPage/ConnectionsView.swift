//
//  ConnectionsView.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-06-20.
//

import SwiftUI

struct ConnectionsView : View {
    
    
    var body : some View {
        LazyVStack {
            Text("Connections")
        }.frame(
            minWidth: 0,
            maxWidth: .infinity,
            minHeight: 0,
            maxHeight: .infinity,
            alignment: .topLeading
        ).overlay(alignment: .bottomTrailing) {
            NavigationLink (destination: ConnectView()) {
                Image(systemName: "plus")
                  //Add the following modifiers for a circular button.
                  .font(.title.weight(.semibold))
                  .padding()
                  .background(Color.purple)
                  .foregroundColor(.white)
                  .clipShape(Circle())
            }
            //Apply padding to the button to position it correctly.
            .padding()
        }
    }
}
