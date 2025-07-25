//
//  ProfileImageButton.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-09.
//

import SwiftUI
import PhotosUI

struct ProfileImagePicker: View {
    let url: String?
    
    @State var selectedImage: UIImage?
    @State var selectedItem: PhotosPickerItem?
    
    var onChange: (UIImage) -> Void
    
    var body: some View {
        PhotosPicker(selection: $selectedItem, matching: .images, photoLibrary: .shared()){
            if let image = selectedImage {
                Image(uiImage: image)
                    .resizable()
                    .scaledToFill()
                    .frame(width: 150, height: 150)
                    .clipShape(Circle())
                    .overlay(Circle().stroke(Color.black, lineWidth: 2))
                    .shadow(radius: 5)
                    .padding()
            } else if let imageUrl = url {
                ProfileImage(url: imageUrl, size: 150)
            }
            else {
                Image(systemName: "person.circle.fill")
                    .resizable()
                    .padding()
                    .scaledToFit()
                    .frame(width: 150, height: 150)
            }
        }.onChange(of: selectedItem) {
            Task {
                if let data = try? await selectedItem?.loadTransferable(type: Data.self),
                   let uiImage = UIImage(data: data)
                {
                    self.selectedImage = uiImage
                    onChange(uiImage)
                }
            }
        }
    }
}

#Preview {
    ProfileImagePicker(url: "https://picsum.photos/150", selectedImage: nil, selectedItem: nil, onChange: { _ in})
}
