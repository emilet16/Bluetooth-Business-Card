//
//  PFPImage.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-04.
//

import SwiftUI

struct ProfileImage: View {
    let url: String?
    var size: CGFloat = 60
    
    var body: some View {
        if let imageLink = url {
            AsyncImage(url: URL(string: imageLink)) { phase in
                switch phase {
                case .empty:
                    ProgressView()
                        .frame(width: size, height: size)
                case .success(let image):
                    image
                        .resizable()
                        .scaledToFill()
                        .frame(width: size, height: size)
                        .clipShape(Circle())
                        .overlay(Circle().stroke(Color.black, lineWidth: 2))
                        .shadow(radius: 5)
                case .failure:
                    Image(systemName: "person.circle.fill")
                        .resizable()
                        .scaledToFit()
                        .frame(width: size, height: size)
                        .foregroundStyle(.gray)
                @unknown default:
                    EmptyView()
                }
            }
        } else {
            Image(systemName: "person.circle.fill")
                .resizable()
                .scaledToFit()
                .frame(width: size, height: size)
                .foregroundStyle(.gray)
        }
    }
}

#Preview {
    ProfileImage(url: "https://picsum.photos/60", size: 60)
}
