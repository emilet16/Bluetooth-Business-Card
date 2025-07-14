//
//  LoadingView.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-02.
//


import SwiftUI

struct LoadingView : View {
    var body : some View {
        VStack {
            Text("Loading...").font(.roboto(17))
        }.background(Color.clear)
    }
}

#Preview {
    LoadingView()
}
