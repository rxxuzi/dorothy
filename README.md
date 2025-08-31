# Dorothy

Dorothy is Free software for hiding encrypted messages in PNG images to protect digital privacy.

## Features

- **Hybrid Encryption**: AES-256-GCM + RSA-2048 for unlimited text size
- **PNG Steganography**: Hide encrypted data in PNG image files using tEXt chunks
- **Modern GUI**: Intuitive Swing-based interface with Dorothy theme
- **Key Management**: RSA key generation, import/export functionality
- **Real-time Editing**: Live text editing with auto-save support
- **Drag & Drop**: Easy file loading with drag-and-drop support

## Security

- **Industry Standard Encryption**: AES-256-GCM with authenticated encryption
- **RSA Key Exchange**: 2048-bit RSA for secure key distribution
- **No Size Limitations**: Encrypt text of any length using hybrid cryptography
- **PNG Format Compliance**: Maintains valid PNG structure with proper CRC

## Requirements

- Java 11+

## Usage

1. **Load Image**: Drag & drop or open a PNG file
2. **Add Text**: Create new text chunks with custom keywords
3. **Encrypt Data**: Secure your text with hybrid encryption (no size limits)
4. **Save File**: Export your modified PNG with hidden data

## Key Features

### Encryption Modes
- **Hybrid AES+RSA**: Default mode for unlimited text size
- **Legacy Support**: Backward compatibility with older encrypted files

### User Interface
- **Chunk Management**: Visual list of all text chunks
- **Text Editor**: Syntax-highlighted editor with real-time updates
- **Image Preview**: View your PNG images with zoom controls
- **Quick Actions**: Fast access to common operations

### Security Features
- **Key Generation**: Automatic RSA key pair creation
- **Key Import/Export**: Share public keys for secure communication
- **Encrypted Storage**: All sensitive data properly encrypted before storage

## Architecture

- **Pure Java**: No external dependencies, uses standard Java cryptography
- **MVC Pattern**: Clean separation of model, view, and controller
- **Centralized Styling**: Consistent UI through DoroStyle system
- **Modular Design**: Clear component separation for maintainability

## License

[AGPL-3.0](LICENSE)
