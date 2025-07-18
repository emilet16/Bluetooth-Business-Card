//
//  ImageManager.swift
//  Connect
//
//  Created by Emile Turcotte on 2025-07-09.
//

import PhotosUI
import SDWebImage
import SDWebImageWebPCoder

protocol ImageRepository  {
    func resizeTo400(image: UIImage) -> UIImage
    func encodeToWebP(image: UIImage) -> Data
}

class ImageManager : ImageRepository {
    func resizeTo400(image: UIImage) -> UIImage {
        let size = image.size
        
        let scale = max(400.0/size.width, 400.0/size.height)
        
        let newSize = CGSize(width: size.width*scale, height: size.height*scale)
        
        let format = UIGraphicsImageRendererFormat.default()
        format.scale = 1
        
        let renderer = UIGraphicsImageRenderer(size: CGSize(width: 400, height: 400), format: format)
        return renderer.image { context in
            let origin = CGPoint(x: (400 - newSize.width)/2, y: (400 - newSize.height)/2)
            image.draw(in: CGRect(origin: origin, size: newSize))
        }
    }
    
    func encodeToWebP(image: UIImage) -> Data {
        let webpCoder = SDImageWebPCoder.shared
        let options: [SDImageCoderOption: Any] = [
            .encodeWebPLossless: false,
            .encodeCompressionQuality: 0.8
        ]
        return webpCoder.encodedData(with: image, format: .webP, options: options)!
    }
}
