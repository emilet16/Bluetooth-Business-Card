//
//  MockImageRepo.swift
//  Quartier
//
//  Created by Emile Turcotte on 2025-07-23.
//

import PhotosUI
import Testing
@testable import Quartier

//Encode and decode the image path to bypass this in the tests

final class MockImageRepo : ImageRepository {
    func resizeTo400(image: UIImage) -> UIImage {
        return image
    }
    
    func encodeToWebP(image: UIImage) -> Data {
        return image.pngData()!
    }
}
